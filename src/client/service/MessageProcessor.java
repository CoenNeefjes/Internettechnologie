package client.service;

import client.ClientApplication;
import client.gui.ClientGui;
import client.gui.LoginScreen;
import general.CryptographyHandler;
import general.MessageBase64Handler;
import general.MessageHandler;
import general.MessageMD5Encoder;
import general.MsgType;
import general.RandomString;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MessageProcessor extends MessageHandler implements Runnable {

  private ClientGui clientGui;
  private LoginScreen loginScreen;

  private CopyOnWriteArrayList<String> sentCommands = new CopyOnWriteArrayList<>();

  private Cipher encryptCipher;
  private Cipher decryptCipher;

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

  public void sendPrivateMessage(String messageWithoutMessageType) {
    sendMessage(MsgType.PMSG + " " + encrypt(messageWithoutMessageType));
  }

  public void sendMessage(String message) {
    System.out.println("Sending message: " + message);
    writer.println(message);
    writer.flush();
    sentCommands.add(message);
  }

  @Override
  protected void handleHelloMessage(String line) {
    loginScreen = new LoginScreen(this);
    loginScreen.setVisible(true);
  }

  @Override
  protected void handleQuitMessage() throws IOException {
    socket.close();
    System.exit(0);
  }

  @Override
  protected void handleBroadCastMessage(String line) {
    line = line.substring(5); // Remove the prefix
    String sender = line.split(" ")[0];
    String message = line.substring(sender.length() + 1);
    clientGui.receiveMessage(MsgType.BCST, sender, message);
  }

  @Override
  protected void handleClientListMessage(String line) {
    ClientApplication.clientNames = new HashSet<>(Arrays.asList(line.substring(5).split(", ")));
    clientGui.updateClientList();
  }

  @Override
  protected void handlePrivateMessage(String line) {
    line = decrypt(line.substring(5));
    System.out.println("decrypted private message: " + line);
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
    clientGui.receiveMessage(MsgType.GMSG, groupName, sender, message);
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
      clientGui
          .receiveMessage(MsgType.KGCL, "Server", parts[1] + " was kicked from group " + parts[0]);
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
    if (clientGui != null) {
      clientGui.errorBox(msg);
    } else {
      loginScreen.errorBox(msg);
    }
  }

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
            initClientGui(command.substring(5));
            initEncryption();
            break;
          case "FILE":
            clientGui.receiveMessage(MsgType.FILE, "Server", "Successfully sent the file");
            break;
          case "PMSG":
            String decrypted = decrypt(parts[1]);
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

  @Override
  protected void handleCryptoKeyMessage(String key) {
    // Client should not receive cryptoKey message
    System.out.println("Client received CRYP message, this should not happen");
  }

  @Override
  protected void handleFileMessage(String line) {
    String[] parts = line.substring(5).split(" ");
    String filePath = ClientApplication.DOWNLOAD_LOCATION + parts[1];
    String fileString = MessageBase64Handler.decode(parts[2]);

    try (FileOutputStream stream = new FileOutputStream(filePath)) {
      stream.write(fileString.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }

    clientGui.receiveMessage(MsgType.FILE, "Server",
        "You received a file from " + parts[0] + ". It is saved at: " + filePath);
  }

  public void shareFile(String recipient, String filePath) {
//    filePath = "C:/Users/Coen Neefjes/IdeaProjects/Internettechnologie/src/files/upload.txt";
    String[] parts = filePath.split("/");
    String fileName = parts[parts.length - 1];

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
      System.out.println("Sending " + filePath + "(" + fileBytes.length + " bytes)");
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

  private String decrypt(String encryptedMessage) {
    return CryptographyHandler.decrypt(decryptCipher, encryptedMessage);
  }

  private String encrypt(String plainText) {
    return CryptographyHandler.encrypt(encryptCipher, plainText);
  }

  private void initEncryption() {
    try {
      // Generate key and initialisation vector
      RandomString randomStringGenerator = new RandomString();
      String key = randomStringGenerator.nextString();
      String initVector = randomStringGenerator.nextString();
      // Create the ciphers
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
      encryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
      decryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
      // Send the key and iv to the server
      sendMessage(MsgType.CRYP + " " + key + " " + initVector);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    }
  }

  private void initClientGui(String userName) {
    clientGui = new ClientGui(this);
    clientGui.setUserName(userName);
    clientGui.setTitle(userName);
    clientGui.setVisible(true);
    clientGui.setRecipient("All");
  }

}
