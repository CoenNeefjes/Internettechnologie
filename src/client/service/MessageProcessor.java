package client.service;

import client.ClientApplication;
import client.gui.ClientGui;
import client.gui.LoginScreen;
import general.CryptographyHandler;
import general.MessageBase64Handler;
import general.MessageHandler;
import general.MessageMD5Encoder;
import general.MsgType;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class that extends the MessageHandler and implements its methods for the client
 *
 * @author Coen Neefjes
 */
public class MessageProcessor extends MessageHandler implements Runnable {

  // User interfaces
  private ClientGui clientGui;
  private LoginScreen loginScreen;

  // Keep track of the commands we sent
  private CopyOnWriteArrayList<String> sentCommands = new CopyOnWriteArrayList<>();

  // Used for encrypting and decrypting private messages
  private CryptographyHandler cryptographyHandler;

  public MessageProcessor(Socket serverSocket) throws IOException {
    super(serverSocket);
  }

  @Override
  public void run() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    while (socket.isConnected() && !socket.isClosed()) {
      receiveMessage(reader);
    }
  }

  /**
   * Send a private message
   *
   * @param messageWithoutMessageType The message
   */
  public void sendPrivateMessage(String messageWithoutMessageType) {
    sendMessage(MsgType.PMSG + " " + cryptographyHandler.encrypt(messageWithoutMessageType));
  }

  /**
   * Sends any message
   *
   * @param message The message
   */
  public void sendMessage(String message) {
    System.out.println("Sending message: " + message);
    writer.println(message);
    writer.flush();
    sentCommands.add(message);
  }

  /**
   * Opens the login screen when receiving a hello message
   *
   * @param line The message
   */
  @Override
  protected void handleHelloMessage(String line) {
    loginScreen = new LoginScreen(this);
    loginScreen.setVisible(true);
  }

  /**
   * Closes the application when receiving a quit message
   */
  @Override
  protected void handleQuitMessage() throws IOException {
    socket.close();
    System.exit(0);
  }

  /**
   * Prints a broadcast message on the clientGui
   *
   * @param line The broadcast message
   */
  @Override
  protected void handleBroadCastMessage(String line) {
    line = line.substring(5); // Remove the prefix
    String sender = line.split(" ")[0];
    String message = line.substring(sender.length() + 1);
    clientGui.receiveMessage(MsgType.BCST, sender, message);
  }

  /**
   * Updates the client list on the clientGui when receiving a client list message
   *
   * @param line The client list message
   */
  @Override
  protected void handleClientListMessage(String line) {
    ClientApplication.clientNames = new HashSet<>(Arrays.asList(line.substring(5).split(", ")));
    clientGui.updateClientList();
  }

  /**
   * Decodes and prints a private message on the clientGui when receiving a private message
   *
   * @param line The private message
   */
  @Override
  protected void handlePrivateMessage(String line) {
    line = cryptographyHandler.decrypt(line.substring(5));
    System.out.println("decrypted private message: " + line);
    String sender = line.split(" ")[0];
    String message = line.substring(sender.length() + 1);
    clientGui.receiveMessage(MsgType.PMSG, sender, message);
  }

  /**
   * Handles receiving a create group message
   *
   * @param line The create group message
   */
  @Override
  protected void handleCreateGroupMessage(String line) {
    // Client should not receive create group message
    System.out.println("Client received CGRP message, this should not happen");
  }

  /**
   * Updates the group list on the clientGui when receiving a group list message
   *
   * @param line The group list message
   */
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

  /**
   * Handles receiving a join group message
   *
   * @param line The join group message
   */
  @Override
  protected void handleJoinGroupMessage(String line) {
    // Client should not receive join group message
    System.out.println("Client received JGRP message, this should not happen");
  }

  /**
   * Prints a group message on the clientGui when receiving a group message
   *
   * @param line The group message
   */
  @Override
  protected void handleGroupMessage(String line) {
    line = line.substring(5);
    String groupName = line.split(" ")[0];
    String sender = line.split(" ")[1];
    String message = line.substring(groupName.length() + sender.length() + 2);
    clientGui.receiveMessage(MsgType.GMSG, groupName, sender, message);
  }

  /**
   * Prints a message on the clientGui that a user left a group
   *
   * @param line The leave group message
   */
  @Override
  protected void handleLeaveGroupMessage(String line) {
    String[] parts = line.substring(5).split(" ");
    clientGui.receiveMessage(MsgType.LGRP, "Server", parts[1] + " left group " + parts[0]);
  }

  /**
   * Prints a message on the clientGui that a user was kicked from a group
   *
   * @param line The kick group client message
   */
  @Override
  protected void handleKickGroupClientMessage(String line) {
    // Remove message type
    line = line.substring(5);
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
      clientGui
          .receiveMessage(MsgType.KGCL, "Server", parts[1] + " was kicked from group " + parts[0]);
    }
  }

  /**
   * Sends a PONG message upon receiving a PING message
   */
  @Override
  protected void handlePingMessage() {
    writer.println("PONG");
    writer.flush();
  }

  /**
   * Handles receiving a PONG message
   */
  @Override
  protected void handlePongMessage() {
    // Client should not receive pong message
    System.out.println("Client received PONG message, this should not happen");
  }

  /**
   * Shows an error box when receiving an error message
   *
   * @param msg The error message
   */
  @Override
  protected void handleErrorMessage(String msg) {
    if (clientGui != null) {
      clientGui.errorBox(msg);
    } else {
      loginScreen.errorBox(msg);
    }
  }

  /**
   * Updates the GUI when receiving a confirmation of a previously sent message. This method ensures
   * the application does not process commands that are not accepted by the server first
   */
  @Override
  protected void handleOkMessage(String line) {
    for (String command : sentCommands) {
      if (MessageMD5Encoder.encode(command).equals(line)) {
        String[] parts = command.split(" ");
        switch (parts[0]) {
          case "JGRP":
            ClientApplication.subscribedGroups.add(command.substring(5));
            clientGui.updateGroupList();
            break;
          case "CGRP":
            ClientApplication.subscribedGroups.add(command.substring(5));
            ClientApplication.myGroups.add(command.substring(5));
            clientGui.updateGroupList();
            break;
          case "LGRP":
            ClientApplication.subscribedGroups.remove(command.substring(5));
            ClientApplication.myGroups.remove(command.substring(5));
            clientGui.updateGroupList();
            break;
          case "HELO":
            loginScreen.setVisible(false);
            initEncryption();
            initClientGui(command.substring(5));
            break;
          case "FILE":
            clientGui.receiveMessage(MsgType.FILE, "Server", "Successfully sent the file");
            break;
          case "PMSG":
            String decrypted = cryptographyHandler.decrypt(parts[1]);
            String name = decrypted.split(" ")[0];
            clientGui.receiveMessage(MsgType.PMSG, "You to " + name,
                decrypted.substring(name.length() + 1));
            break;
        }
        sentCommands.remove(command);
        return;
      }
    }
    System.out.println("Unknown +OK message received");
  }

  /**
   * Handles receiving a crypto key message
   *
   * @param key The crypto key message
   */
  @Override
  protected void handleCryptoKeyMessage(String key) {
    // Client should not receive cryptoKey message
    System.out.println("Client received CRYP message, this should not happen");
  }

  /**
   * Saves a file to the standard given location when receiving a file message and prints this on
   * the clientGui
   *
   * @param line The file message
   */
  @Override
  protected void handleFileMessage(String line) {
    String[] parts = line.substring(5).split(" ");
    String filePath = ClientApplication.DOWNLOAD_LOCATION + parts[1];
    String fileString = MessageBase64Handler.decode(parts[2]);

    System.out.println("filePath: " + filePath);

    //TODO: create file before writing, otherwise error

//    try (FileOutputStream stream = new FileOutputStream(filePath)) {
//      stream.write(fileString.getBytes());
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

//    Writer writer = null;
//    try {
//      writer = new BufferedWriter(new OutputStreamWriter(
//          new FileOutputStream(filePath), "utf-8"));
//      writer.write(fileString);
//    } catch (IOException e) {
//      e.printStackTrace();
//    } finally {
//      try {writer.close();} catch (Exception ex) {/*ignore*/}
//    }

    try {
      Files.write(Paths.get(filePath), fileString.getBytes("utf-8"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    clientGui.receiveMessage(MsgType.FILE, "Server",
        "You received a file from " + parts[0] + ". It is saved at: " + filePath);
  }

  /**
   * Sends a file message
   *
   * @param recipient The recipient name
   * @param filePath The filePath of the file to be sent
   */
  public void shareFile(String recipient, String filePath) {
    String fileName = new File(filePath).getName();

    FileInputStream fileInputStream = null;
    BufferedInputStream bufferedInputStream = null;
    try {
      File myFile = new File(filePath);
      byte[] fileBytes = new byte[(int) myFile.length()];
      fileInputStream = new FileInputStream(myFile);
      bufferedInputStream = new BufferedInputStream(fileInputStream);
      bufferedInputStream.read(fileBytes, 0, fileBytes.length);
      sendMessage("FILE " + recipient + " " + fileName + " " + MessageBase64Handler
          .encode(new String(fileBytes)));
      System.out.println("Sending file at path: " + filePath + "(" + fileBytes.length + " bytes)");
    } catch (IOException e) {
      e.printStackTrace();
      clientGui.errorBox("Could not find or send specified file");
    } finally {
      try {
        if (fileInputStream != null) {
          fileInputStream.close();
        }
        if (bufferedInputStream != null) {
          bufferedInputStream.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  /**
   * Initializes the encryption and sends the key and initialisation vector to the server
   */
  private void initEncryption() {
    cryptographyHandler = new CryptographyHandler();
    sendMessage(MsgType.CRYP + " " + cryptographyHandler.getKey() + " " + cryptographyHandler
        .getInitVector());
  }

  /**
   * Initialises the client gui
   *
   * @param userName The username of this client
   */
  private void initClientGui(String userName) {
    clientGui = new ClientGui(this);
    clientGui.setUserName(userName);
    clientGui.setTitle(userName);
    clientGui.setVisible(true);
    clientGui.setRecipient("All");
  }

}
