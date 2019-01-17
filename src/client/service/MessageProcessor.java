package client.service;

import client.ClientApplication;
import client.gui.ClientGui;
import client.gui.LoginScreen;
import general.MessageHandler;

import general.MessageMD5Encoder;
import general.MsgType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import server.Server;

public class MessageProcessor extends MessageHandler implements Runnable {

  private ClientGui clientGui;

  private CopyOnWriteArrayList<String> sentCommands = new CopyOnWriteArrayList<>();

  public MessageProcessor(Socket serverSocket) throws IOException {
    super(serverSocket);
  }

  @Override
  public void run() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    while (socket.isConnected()) {
      receiveMessage(reader);
    }
  }

  public void sendMessage(String msg) {
    writer.println(msg);
    writer.flush();
    sentCommands.add(msg);
  }

  @Override
  protected void handleHelloMessage(String line) {
    clientGui = new ClientGui(this);
    LoginScreen loginScreen = new LoginScreen(writer, (userName) -> {
      clientGui.setUserName(userName);
      clientGui.setTitle(userName);
      clientGui.setVisible(true);
      clientGui.setRecipient("All");
    });
    loginScreen.setVisible(true);
  }

  @Override
  protected void handleQuitMessage() {
    // Client should not receive quit message
    System.out.println("Client received QUIT message, this should not happen");
  }

  @Override
  protected void handleBroadCastMessage(String line) {
    line = line.substring(5); // Remove the prefix
    String sender = line.split(" ")[0];
    String message = line.substring(sender.length()+1);
    clientGui.receiveMessage(MsgType.BCST, sender, message);
  }

  @Override
  protected void handleClientListMessage(String line) {
    Set<String> clients = new HashSet<>(Arrays.asList(line.substring(5).split(", ")));
    ClientApplication.clientNames.addAll(clients);
    clientGui.updateClientList();


    System.out.println("Online clients: " + line.substring(5));
  }

  @Override
  protected void handlePrivateMessage(String line) {
    String sender = line.split(" ")[0];
    String message = line.substring(sender.length() + 1);
    clientGui.receiveMessage(MsgType.PMSG, sender, message);
  }

  @Override
  protected void handleCreateGroupMessage(String line) {
    // Client should not receive create group message
    System.out.println("Client received CGRP message, this should not happen");
  }

  @Override
  protected void handleGroupListMessage(String line) {
    if (line.length() < 5) {
      // There are no more groups
      ClientApplication.groupNames.clear();
      ClientApplication.subscribedGroups.clear();
      ClientApplication.myGroups.clear();
    } else {
      // Replace all groups with current information
      ClientApplication.groupNames = new HashSet<>(Arrays.asList(line.substring(5).split(", ")));
      // Check if our locally followed groups still exist
      ClientApplication.subscribedGroups.retainAll(ClientApplication.groupNames);
      ClientApplication.myGroups.retainAll(ClientApplication.groupNames);
    }
    clientGui.updateGroupList();
  }

  @Override
  protected void handleJoinGroupMessage(String line) {
    // Client should not receive join group message
    System.out.println("Client received JGRP message, this should not happen");
  }

  @Override
  protected void handleGroupMessage(String line) {
    String groupName = line.split(" ")[0];
    String sender = line.split(" ")[1];
    String message = line.substring(groupName.length() + sender.length() + 2);
    clientGui.receiveMessage(MsgType.PMSG, groupName, sender, message);
  }

  @Override
  protected void handleLeaveGroupMessage(String line) {
    String[] parts = line.substring(5).split(" ");
    clientGui.receiveMessage(MsgType.LGRP, "Server", parts[1] + " left group " + parts[0]);
  }

  @Override
  protected void handleKickGroupClientMessage(String line) {
    // Client should not receive kick group client message
    System.out.println("KGCL message=" + line);
    String[] parts = line.split(" ");
    if (parts.length == 1) {
      // We have been kicked from a group
      ClientApplication.subscribedGroups.remove(line);
      clientGui.updateGroupList();
      clientGui.receiveMessage(MsgType.KGCL, "Server", "You were kicked from group " + parts[0]);
    } else {
      // Someone else has been kicked from a group
      clientGui.receiveMessage(MsgType.KGCL, "Server", parts[1] + " was kicked from group " + parts[0]);
    }
  }

  @Override
  protected void handlePingMessage() {
    writer.println("PONG");
    writer.flush();
  }

  @Override
  protected void handlePongMessage() {
    // Client should not receive pong message
    System.out.println("Client received PONG message, this should not happen");
  }

  @Override
  protected void handleErrorMessage(String msg) {
    clientGui.errorBox(msg);
  }

  @Override
  protected void handleOkMessage(String line) {
    for (String command: sentCommands) {
      if (MessageMD5Encoder.encode(command).equals(line)) {
        switch (command.split(" ")[0]) {
          case "JGRP":
            ClientApplication.subscribedGroups.add(command.substring(5));
            break;
          case "CGRP":
            ClientApplication.subscribedGroups.add(command.substring(5));
            ClientApplication.myGroups.add(command.substring(5));
            break;
          case "LGRP":
            ClientApplication.subscribedGroups.remove(command.substring(5));
            ClientApplication.myGroups.remove(command.substring(5));
            break;
        }
        sentCommands.remove(command);
        clientGui.updateGroupList();
      }
    }
  }

}
