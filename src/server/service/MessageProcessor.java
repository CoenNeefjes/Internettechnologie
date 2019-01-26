package server.service;

import general.MessageHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import server.Server;
import server.model.Client;
import server.model.Group;
import server.util.ErrorMessageConstructor;
import server.util.MessageConstructor;
import server.util.StringValidator;

/**
 * Class that extends the MessageHandler and implements its methods for the client
 *
 * @author Coen Neefjes
 */
public class MessageProcessor extends MessageHandler implements Runnable {

  private Client client;

  public MessageProcessor(Socket clientSocket) throws IOException {
    super(clientSocket);
  }

  @Override
  public void run() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    while (socket.isConnected() && !socket.isClosed()) {
      receiveMessage(reader);
    }
  }

  /**
   * Sends a message to the client of which the writer belongs to
   *
   * @param message The message String that needs to be sent
   * @param writer The writer of the client the message should go to
   */
  private void sendMessage(String message, PrintWriter writer) {
    System.out.println("Sending message: " + message);
    writer.println(message);
    writer.flush();
  }

  /**
   * Sends an +OK message to the client of this MessageProcessor
   *
   * @param line The line that needs to be converted to an +OK message
   */
  private void returnOkMessage(String line) {
    sendMessage(MessageConstructor.okMessage(line), writer);
  }

  /**
   * Sends a message to all connected clients
   *
   * @param line The message
   */
  private void broadcastMessage(String line) throws IOException {
    for (Client client : Server.clients) {
      Socket socket = client.getClientSocket();
      sendMessage(MessageConstructor.broadcastMessage(this.client.getName(), line),
          new PrintWriter(socket.getOutputStream()));
    }
  }

  /**
   * Handles the receiving of a HELO message If the message is validated this method adds this
   * client to the client list on the server and returns an +OK message and starts a heartBeat
   * Thread and sends every connected client a new version of the client list and sends this client
   * a list of available groups
   *
   * @param line The message
   */
  @Override
  protected void handleHelloMessage(String line) {
    try {
      if (client != null) {
        sendMessage(ErrorMessageConstructor.alreadyLoggedInError(), writer);
        return;
      }

      String userName = line.substring(5);
      if (!StringValidator.validateNameString(userName)) {
        sendMessage(ErrorMessageConstructor.invalidNameError(userName), writer);
        return;
      }

      if (!Server.getUserNames().contains(userName)) {
        this.client = new Client(socket, userName);
        Server.clients.add(client);
        returnOkMessage(line);
        // Start heartbeat Thread
        Thread heartbeatThread = new Thread(new PingPong(client));
        heartbeatThread.start();
        // Send an updated clientList to every user
        broadCastClientList();
        // Send a list of available groups to the new user
        handleGroupListMessage(null);
      } else {
        sendMessage(ErrorMessageConstructor.alreadyLoggedInError(), writer);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Receives a BCST message and sends it to all connected clients
   *
   * @param line The message
   */
  @Override
  protected void handleBroadCastMessage(String line) {
    try {
      if (StringValidator.validateMessageString(line.substring(5))) {
        returnOkMessage(line);
        broadcastMessage(line.substring(5));
      } else {
        sendMessage(ErrorMessageConstructor.emptyMessageError(), writer);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Receives a QUIT message and returns a QUIT message Then it removes this client from all groups
   * and the client list on the server After that it sends the new client list to all connected
   * clients
   */
  @Override
  protected void handleQuitMessage() throws IOException {
    // Send response QUIT message to the user
    sendMessage(MessageConstructor.quitMessage(), writer);
    // Remove client from groups
    Server.groups.forEach(group -> {
      if (group.getGroupMembers().contains(client)) {
        handleLeaveGroupMessage("LGRP " + group.getName());
      }
    });
    // Remove the client from the server
    Server.clients.remove(client);
    // Send an updated clientList to every user
    broadCastClientList();
    // Close the connection
    socket.close();
  }

  /**
   * Sends the list of connected clients to this client
   *
   * @param line The client list message
   */
  @Override
  protected void handleClientListMessage(String line) {
    String clientListString = "";
    for (Client client : Server.clients) {
      clientListString += client.getName() + ", ";
    }
    sendMessage(MessageConstructor.clientListMessage(clientListString), writer);
  }

  /**
   * Receives a private message. Decrypts it. Then encrypts it with the Cipher of the person it is
   * to be sent to. Then sends the message to that person.
   *
   * @param line The encrypted private message
   */
  @Override
  protected void handlePrivateMessage(String line) {
    // Decrypt the message
    String decryptedLine = client.getDecryptedMessage(line.substring(5));

    // Get the message info
    String recipientName = decryptedLine.split(" ")[0];
    String message = decryptedLine.substring(recipientName.length() + 1);

    // Check if not sending to self
    if (!recipientName.equals(this.client.getName())) {
      // Check if the message is not empty
      if (StringValidator.validateMessageString(message)) {
        // Try to get the client
        Client client = Server.getClientByName(recipientName);
        if (client != null) {
          try {
            // If client exists send message
            Socket socket = client.getClientSocket();
            sendMessage(MessageConstructor.encryptedPrivateMessage(client.encryptMessage(
                this.client.getName() + " " + message)),
                new PrintWriter(socket.getOutputStream()));
            returnOkMessage(line);
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          sendMessage(ErrorMessageConstructor.clientNotFoundError(), writer);
        }
      } else {
        System.out.println("Empty message: " + message);
        sendMessage(ErrorMessageConstructor.emptyMessageError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.sendToSelfError(), writer);
    }

  }

  /**
   * Creates a new group upon receiving a create group message
   *
   * @param line The create group message
   */
  @Override
  protected void handleCreateGroupMessage(String line) {
    // Remove the message prefix
    String groupName = line.substring(5);

    // Check if the group name is valid
    if (!StringValidator.validateNameString(groupName)) {
      sendMessage(ErrorMessageConstructor.invalidNameError(groupName), writer);
      return;
    }
    if (!Server.getGroupNames().contains(groupName)) {
      // Create the group
      Server.groups.add(new Group(groupName, client));
      returnOkMessage(line);
      // Send every client an updated group list
      broadCastGroupList();
    } else {
      sendMessage(ErrorMessageConstructor.groupNameAlreadyExistsError(), writer);
    }
  }

  /**
   *
   * @param line
   */
  @Override
  protected void handleGroupListMessage(String line) {
    String groupListString = "";
    for (Group group : Server.groups) {
      groupListString += group.getName() + ", ";
    }
    sendMessage(MessageConstructor.groupListMessage(groupListString), writer);
  }

  /**
   * Lets the send of a join group message join that certain group
   *
   * @param line The join group message
   */
  @Override
  protected void handleJoinGroupMessage(String line) {
    String groupName = line.substring(5);
    Group group = Server.getGroupByName(groupName);
    // Check if the group exists
    if (group != null) {
      // Check if the client is not already in that group
      if (!group.getGroupMemberNames().contains(client.getName())) {
        group.addGroupMember(client);
        returnOkMessage(line);
      } else {
        sendMessage(ErrorMessageConstructor.clientAlreadyInGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  /**
   * Receives a group message and sends it to all members of this group
   *
   * @param line The group message
   */
  @Override
  protected void handleGroupMessage(String line) {
    // Get the message info
    String groupName = line.substring(5).split(" ")[0];
    String message = line.substring(5).substring(groupName.length() + 1);

    // Try to get the group
    Group group = Server.getGroupByName(groupName);

    // If the group exists, proceed. Otherwise send error
    if (group != null) {
      // Check if this user is in the group
      if (group.getGroupMemberNames().contains(client.getName())) {
        // Check if the message is not empty
        if (StringValidator.validateMessageString(message)) {
          // Return OK message
          returnOkMessage(line);
          // Get all clients from group
          group.getGroupMembers().forEach(groupMember -> {
            try {
              // Send the message to all group members
              Socket socket = groupMember.getClientSocket();
              sendMessage(MessageConstructor.groupMessage(groupName, client.getName(), message),
                  new PrintWriter(socket.getOutputStream()));
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
        } else {
          sendMessage(ErrorMessageConstructor.emptyMessageError(), writer);
        }
      } else {
        sendMessage(ErrorMessageConstructor.clientNotInGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  /**
   * Removes a client from a group when receiving a leave group message
   *
   * @param line The leave group message
   */
  @Override
  protected void handleLeaveGroupMessage(String line) {
    String groupName = line.substring(5);
    Group group = Server.getGroupByName(groupName);
    if (group != null) {
      // Check if user was in group
      if (group.getGroupMemberByName(client.getName()) != null) {
        // If the group owner leaves or the group becomes empty, destroy group
        if (group.isOwner(client) || group.getGroupMembers().isEmpty()) {
          // Destroy the group
          Server.groups.remove(group);
          // Send every user the updated group list
          broadCastGroupList();
        }
        // Remove this client from the group
        group.removeGroupMember(client);
        // Send a message that it was handled correctly
        returnOkMessage(line);
        // Notify all other group members of leave
        notifyGroupOfLeave(group, client.getName(), true);
      } else {
        sendMessage(ErrorMessageConstructor.clientNotInGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  /**
   * Kicks a client from a group when this message was sent by the group owner
   *
   * @param line The kick group client message
   */
  @Override
  protected void handleKickGroupClientMessage(String line) {
    String[] parts = line.substring(5).split(" ");
    if (parts.length != 2) {
      sendMessage(ErrorMessageConstructor.invalidInputError(), writer);
      return;
    }
    String groupName = parts[0];
    String clientName = parts[1];

    // Try to get the group
    Group group = Server.getGroupByName(groupName);
    if (group != null) {
      // Check if this user is group owner
      if (group.isOwner(this.client)) {
        // Check if the user is not kicking himself
        if (!clientName.equals(this.client.getName())) {
          // Check if the user that needs to be kicked is in the group
          Client client = group.getGroupMemberByName(clientName);
          if (client != null) {
            // Return OK message
            returnOkMessage(line);
            // Kick the client
            group.removeGroupMember(client);
            notifyClientOfKick(client, groupName);
            // Notify all other group members of leave
            notifyGroupOfLeave(group, clientName, false);
          } else {
            sendMessage(ErrorMessageConstructor.clientNotInGroupError(), writer);
          }
        } else {
          sendMessage(ErrorMessageConstructor.kickSelfError(), writer);
        }
      } else {
        sendMessage(ErrorMessageConstructor.notOwnerOfGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  /**
   * Handles receiving a Ping message
   */
  @Override
  protected void handlePingMessage() {
    // Server should not receive ping message
    System.out.println("Server received ping message, this should not happen");
  }

  /**
   * Sets the receivedPong boolean in the client to true upon receiving a Pong from this client
   */
  @Override
  protected void handlePongMessage() {
    Server.getClientByName(client.getName()).setReceivedPong(true);
    System.out.println("Received PONG from " + client.getName());
  }

  /**
   * Handles receiving an Error message
   */
  @Override
  protected void handleErrorMessage(String line) {
    // Server should not receive error message
    System.out.println("Server received error message, this should not happen");
    System.out.println("Error message is: " + line);
  }

  /**
   * Handles receiving an OK message
   */
  @Override
  protected void handleOkMessage(String line) {
    // Server should not receive error message
    System.out.println("Server received ok message, this should not happen");
    System.out.println("OK message is: " + line);
  }

  /**
   * Sets up the encryption / decryption process of this client
   *
   * @param line The crypto key message
   */
  @Override
  protected void handleCryptoKeyMessage(String line) {
    String[] parts = line.substring(5).split(" ");
    client.initEncryption(parts[0], parts[1]);
    returnOkMessage(line);
  }

  /**
   * Receives a file message and forwards it to the client it needs to be sent to
   *
   * @param line The file message
   */
  @Override
  protected void handleFileMessage(String line) {
    String[] parts = line.substring(5).split(" ");
    String recipientName = parts[0];
    Client recipient = Server.getClientByName(recipientName);
    if (!recipientName.equals(client.getName())) {
      if (recipient != null) {
        try {
          Socket socket = recipient.getClientSocket();
          sendMessage(MessageConstructor.fileMessage(client.getName(), parts[1], parts[2]),
              new PrintWriter(socket.getOutputStream()));
          returnOkMessage(line);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        sendMessage(ErrorMessageConstructor.clientNotFoundError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.sendToSelfError(), writer);
    }
  }

  /**
   * Sends the list of connected clients to all connected clients
   */
  private void broadCastClientList() throws IOException {
    String clientListString = "";
    for (Client client : Server.clients) {
      clientListString += client.getName() + ", ";
    }

    for (Client client : Server.clients) {
      Socket socket = client.getClientSocket();
      sendMessage(MessageConstructor.clientListMessage(clientListString),
          new PrintWriter(socket.getOutputStream()));
    }
  }

  /**
   * Sends the list of all existing groups to all connected clients
   */
  private void broadCastGroupList() {
    String groupListString = "";
    for (Group group : Server.groups) {
      groupListString += group.getName() + ", ";
    }

    try {
      for (Client client : Server.clients) {
        Socket socket = client.getClientSocket();
        sendMessage(MessageConstructor.groupListMessage(groupListString),
            new PrintWriter(socket.getOutputStream()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends a message to all group members that a person has left the group
   *
   * @param group The group that is notified
   * @param clientName The name of the client that left
   * @param voluntarily True if the person left by sending a leave group message, false if the
   * person left by a kick group client message
   */
  private void notifyGroupOfLeave(Group group, String clientName, boolean voluntarily) {
    group.getGroupMembers().forEach(groupMember -> {
      try {
        Socket socket = groupMember.getClientSocket();
        String message = voluntarily ?
            MessageConstructor.leaveGroupMessage(group.getName(), clientName) :
            MessageConstructor.notifyGroupOfKickMessage(group.getName(), clientName);
        sendMessage(message, new PrintWriter(socket.getOutputStream()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * Sends a message to the client that he has been kicked from a group
   * @param client The client that was kicked
   * @param groupName The name of the group he was kicked from
   */
  private void notifyClientOfKick(Client client, String groupName) {
    try {
      sendMessage(MessageConstructor.notifyClientOfKickMessage(groupName),
          new PrintWriter(client.getClientSocket().getOutputStream()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
