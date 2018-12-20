package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import server.model.Client;
import server.model.Group;
import server.util.ErrorMessageConstructor;
import server.util.MessageConstructor;

public class MessageProcessor implements Runnable {

  private InputStream inputStream;
  private OutputStream outputStream;
  private Socket clientSocket;

  private Client client;

  private PrintWriter writer;

  public MessageProcessor(Socket clientSocket) throws IOException {
    this.inputStream = clientSocket.getInputStream();
    this.outputStream = clientSocket.getOutputStream();
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    // Start read en write
    writer = new PrintWriter(outputStream);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    // While connected
    while (clientSocket.isConnected()) {
      try {
        String line = "";

        if (reader.ready()) {
          line = reader.readLine();
          System.out.println("Received message: " + line);
        }

        switch (line.split(" ")[0]) {
          case "HELO":
            handleHelloMessage(line);
            break;
          case "QUIT":
            handleQuitMessage();
            break;
          case "BCST":
            returnOkMessage(line);
            broadcastMessage(line.substring(5));
            break;
          case "CLST":
            handleClientListMessage();
            break;
          case "PMSG":
            handlePrivateMessage(line.substring(5));
            break;
          case "CGRP":
            handleCreateGroupMessage(line.substring(5));
            break;
          case "GLST":
            handleGroupListMessage();
            break;
          case "JGRP":
            handleJoinGroupMessage(line.substring(5));
            break;
          case "GMSG":
            handleGroupMessage(line.substring(5));
            break;
          case "LGRP":
            handleLeaveGroupMessage(line);
            break;
          case "LGCL":
            handleKickGroupClientMessage(line.substring(5));
            break;
        }
      } catch (NoSuchAlgorithmException | IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void sendMessage(String msg, PrintWriter writer) {
    writer.println(msg);
    writer.flush();
  }

  private void handleHelloMessage(String line)
      throws IOException, NoSuchAlgorithmException {

    if (client != null) {
      sendMessage(ErrorMessageConstructor.alreadyLoggedInError(), writer);
      clientSocket.close();
      return;
    }

    String userName = line.substring(5);
    //TODO: check if username is valid string

    if (!Server.getUserNames().contains(userName)) {
      this.client = new Client(clientSocket, userName);
      Server.clients.add(client);
      returnOkMessage(line);
      // Start heartbeat Thread
      Thread heartbeatThread = new Thread(new PingPong(clientSocket));
      heartbeatThread.start();
    } else {
      sendMessage(ErrorMessageConstructor.alreadyLoggedInError(), writer);
      clientSocket.close();
    }
  }

  private void handleQuitMessage() throws IOException {
    Server.clients.remove(client);
    sendMessage(MessageConstructor.quitMessage(), writer);
    clientSocket.close();
  }

  private void returnOkMessage(String line)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    sendMessage(MessageConstructor.okMessage(line), writer);
  }

  private void broadcastMessage(String line) throws IOException {
    for (Client client : Server.clients) {
      if (!client.getName().equals(this.client.getName())) {
        Socket clientSocket = client.getClientSocket();
        sendMessage(MessageConstructor.broadcastMessage(this.client.getName(), line),
            new PrintWriter(clientSocket.getOutputStream()));
      }
    }
  }

  private void handleClientListMessage() throws IOException {
    String clientListString = "";
    for (Client client : Server.clients) {
      clientListString += client.getName() + ", ";
    }
    sendMessage(MessageConstructor.clientListMessage(clientListString), writer);
  }

  private void handlePrivateMessage(String line) throws IOException {
    String recipientName = line.split(" ")[0];

    // Try to get the client
    Client client = Server.getClientByName(recipientName);
    if (client != null) {
      // If client exists send message
      Socket clientSocket = client.getClientSocket();
      sendMessage(
          MessageConstructor
              .privateMessage(this.client.getName(),
                  line.substring(recipientName.length() + 1)),
          new PrintWriter(clientSocket.getOutputStream()));
    } else {
      sendMessage(ErrorMessageConstructor.clientNotFoundError(), writer);
    }
  }

  private void handleCreateGroupMessage(String groupName) {
    //TODO: check if groupName is valid string

    if (!Server.getGroupNames().contains(groupName)) {
      Server.groups.add(new Group(groupName, client));
    } else {
      sendMessage(ErrorMessageConstructor.groupNameAlreadyExistsError(), writer);
    }
  }

  private void handleGroupListMessage() {
    String groupListString = "";
    for (Group group : Server.groups) {
      groupListString += group.getName() + ", ";
    }
    sendMessage(MessageConstructor.groupListMessage(groupListString), writer);
  }

  private void handleJoinGroupMessage(String groupName) {
    Group group = Server.getGroupByName(groupName);
    if (group != null) {
      group.addGroupMember(client);
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  private void handleGroupMessage(String line) {
    String groupName = line.split(" ")[0];
    String message = line.substring(groupName.length() + 1);

    System.out.println("HandleGroupMessage: groupName=" + groupName + ", message=" + message);

    // Try to get the group
    Group group = Server.getGroupByName(groupName);

    // If the group exists, proceed. Otherwise send error
    if (group != null) {
      // Check if this user is in the group
      if (group.getGroupMemberNames().contains(client.getName())) {
        // Get all clients from group except for this client
        group.getGroupMembers().stream()
            .filter(groupMember -> !groupMember.getName().equals(client.getName()))
            .forEach(groupMember -> {
              try {
                // Send the message to all group members
                Socket clientSocket = client.getClientSocket();
                sendMessage(MessageConstructor.groupMessage(groupName, client.getName(), message),
                    new PrintWriter(clientSocket.getOutputStream()));
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  private void handleLeaveGroupMessage(String line) {
    String groupName = line.substring(5);
    System.out.println("handleLeaveGroupMessage: groupName=" + groupName);
    Group group = Server.getGroupByName(groupName);
    if (group != null) {
      // Check if user was in group
      if (group.getGroupMemberByName(client.getName()) != null) {
        group.removeGroupMember(client);
        sendMessage(line, writer);
        //TODO: let the group know the user disconnected
      } else {
        sendMessage(ErrorMessageConstructor.userNotInGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }

  private void handleKickGroupClientMessage(String line) {
    String groupName = line.split(" ")[0];
    String clientName = line.substring(groupName.length()+1);

    System.out.println("handleKickGroupClientMessage: groupName=" + groupName + ", clientName=" + clientName);

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
          //TODO: send a message to the client that he is removed
          //TODO: send a message to the group the client is removed
        } else {
          sendMessage(ErrorMessageConstructor.userNotInGroupError(), writer);
        }
      } else {
        sendMessage(ErrorMessageConstructor.notOwnerOfGroupError(), writer);
      }
    } else {
      sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
    }
  }
}
