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

  private void sendMessage(String msg, PrintWriter writer) {
    writer.println(msg);
    writer.flush();
  }

  private void returnOkMessage(String line) {
    sendMessage(MessageConstructor.okMessage(line), writer);
  }

  private void broadcastMessage(String line) throws IOException {
    for (Client client : Server.clients) {
      Socket socket = client.getClientSocket();
      sendMessage(MessageConstructor.broadcastMessage(this.client.getName(), line),
          new PrintWriter(socket.getOutputStream()));
    }
  }

  @Override
  protected void handleHelloMessage(String line) {
    try {
      if (client != null) {
        sendMessage(ErrorMessageConstructor.alreadyLoggedInError(), writer);
        //TODO: not close the connection
        socket.close();
        return;
      }

      String userName = line.substring(5);
      if (!StringValidator.validateString(userName)) {
        sendMessage(ErrorMessageConstructor.invalidNameError(userName), writer);
        //TODO: not close the connection
        socket.close();
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
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void handleBroadCastMessage(String line) {
    try {
      returnOkMessage(line);
      broadcastMessage(line.substring(5));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void handleQuitMessage() {
    try {
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
      Server.receivedPongPerClient.remove(client.getName());
      // Send an updated clientList to every user
      broadCastClientList();
      // Close the connection
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void handleClientListMessage(String line) {
    String clientListString = "";
    for (Client client : Server.clients) {
      clientListString += client.getName() + ", ";
    }
    sendMessage(MessageConstructor.clientListMessage(clientListString), writer);
  }

  @Override
  protected void handlePrivateMessage(String line) {
    String recipientName = line.split(" ")[0];

    // Try to get the client
    Client client = Server.getClientByName(recipientName);
    if (client != null) {
      try {
        // If client exists send message
        Socket socket = client.getClientSocket();
        sendMessage(
            MessageConstructor
                .privateMessage(this.client.getName(),
                    line.substring(recipientName.length() + 1)),
            new PrintWriter(socket.getOutputStream()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      sendMessage(ErrorMessageConstructor.clientNotFoundError(), writer);
    }
  }

  @Override
  protected void handleCreateGroupMessage(String line) {
    String groupName = line.substring(5);
    if (!StringValidator.validateString(groupName)) {
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

  @Override
  protected void handleGroupListMessage(String line) {
    String groupListString = "";
    for (Group group : Server.groups) {
      groupListString += group.getName() + ", ";
    }
    sendMessage(MessageConstructor.groupListMessage(groupListString), writer);
  }

  @Override
  protected void handleJoinGroupMessage(String line) {
    String groupName = line.substring(5);
    Group group = Server.getGroupByName(groupName);
    if (group != null) {
      if (!group.getGroupMemberNames().contains(client.getName())) {
        group.addGroupMember(client);
        returnOkMessage(line);
      } else {
        // Client is already in group
        sendMessage(ErrorMessageConstructor.clientAlreadyInGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  @Override
  protected void handleGroupMessage(String line) {
    String groupName = line.split(" ")[0];
    String message = line.substring(groupName.length() + 1);

    // Try to get the group
    Group group = Server.getGroupByName(groupName);

    // If the group exists, proceed. Otherwise send error
    if (group != null) {
      // Check if this user is in the group
      if (group.getGroupMemberNames().contains(client.getName())) {
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
        sendMessage(ErrorMessageConstructor.clientNotInGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

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

  @Override
  protected void handleKickGroupClientMessage(String line) {
    String groupName = line.split(" ")[0];
    String clientName = line.substring(groupName.length() + 1);

    // Try to get the group
    Group group = Server.getGroupByName(groupName);
    if (group != null) {
      // Check if this user is group owner
      if (group.isOwner(client)) {
        // Check if the user that needs to be kicked is in the group
        Client client = group.getGroupMemberByName(clientName);
        if (client != null) {
          // Kick the client
          group.removeGroupMember(client);
          notifyClientOfKick(client, groupName);
          // Notify all other group members of leave
          notifyGroupOfLeave(group, clientName, false);
        } else {
          sendMessage(ErrorMessageConstructor.clientNotInGroupError(), writer);
        }
      } else {
        sendMessage(ErrorMessageConstructor.notOwnerOfGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  @Override
  protected void handlePingMessage() {
    // Server should not receive ping message
    System.out.println("Server received ping message, this should not happen");
  }

  @Override
  protected void handlePongMessage() {
    Server.receivedPongPerClient.put(client.getName(), true);
  }

  @Override
  protected void handleErrorMessage(String line) {
    // Server should not receive error message
    System.out.println("Server received error message, this should not happen");
    System.out.println("Error message is: " + line);
  }

  @Override
  protected void handleOkMessage(String line) {
    // Server should not receive error message
    System.out.println("Server received ok message, this should not happen");
  }

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

  private void notifyClientOfKick(Client client, String groupName) {
    try {
      sendMessage(MessageConstructor.notifyClientOfKickMessage(groupName), new PrintWriter(client.getClientSocket().getOutputStream()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
