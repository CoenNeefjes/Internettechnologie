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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MessageProcessor extends MessageHandler implements Runnable {

  private ClientGui clientGui;

  private CopyOnWriteArrayList<String> sentCommands = new CopyOnWriteArrayList<>();

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
    clientGui = new ClientGui(this);
    LoginScreen loginScreen = new LoginScreen(this, (userName) -> {
      // Execute this code when the username is sent
      clientGui.setUserName(userName);
      clientGui.setTitle(userName);
      clientGui.setVisible(true);
      clientGui.setRecipient("All");
      initEncryption();
    });
    loginScreen.setVisible(true);
    // Set up the encryption for private messages and send the secret key to the server
  }

  @Override
  protected void handleQuitMessage() throws IOException {
    socket.close();
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
    line = decrypt(line);
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
    clientGui.errorBox(msg);
  }

  @Override
  protected void handleOkMessage(String line) {
    for (String command : sentCommands) {
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
        return;
      }
    }
    System.out.println("Unknown +OK message received");
    //TODO: find out where last unknown +OK comes from
  }

  @Override
  protected void handleCryptoKeyMessage(String key) {
    // Client should not receive cryptoKey message
    System.out.println("Client received CRYP message, this should not happen");
  }

  //TODO: https://www.rgagnon.com/javadetails/java-0542.html
  public void shareFile(String recipient, String filePath) {
    filePath = "C:/Users/Coen Neefjes/Documents/HBO-ICT/module 6/upload.txt";
    try {
      File myFile = new File(filePath);
      byte[] mybytearray = new byte[(int) myFile.length()];
      FileInputStream fileInputStream = new FileInputStream(myFile);
      BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
      bufferedInputStream.read(mybytearray, 0, mybytearray.length);
      OutputStream outputStream = socket.getOutputStream();
      System.out.println("Sending " + filePath + "(" + mybytearray.length + " bytes)");
      String header = "FILE START";
      System.out.println("Header byte size: " + header.getBytes().length);
      outputStream.write(header.getBytes());
      outputStream.write(mybytearray, 0, mybytearray.length);
      String footer = " END";
      System.out.println("Footer byte size: " + footer.getBytes().length);
      outputStream.write(footer.getBytes());
      outputStream.flush();
      System.out.println("Done sending");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String decrypt(String encryptedMessage) {
    return CryptographyHandler.decrypt(decryptCipher, encryptedMessage);
  }

  private String encrypt(String plainText) {
    return CryptographyHandler.encrypt(encryptCipher, plainText);
  }

//  private void initEncryption() {
//    try {
//      // Generate random key
//      KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//      keyGen.init(128);
//      SecretKey secretKey = keyGen.generateKey();
//      System.out.println("key is: " + new String(secretKey.getEncoded()));
//      // Get instance of ciphers
//      encryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
//      decryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
//      // Generate random initialisation vector
//      SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
//      byte[] ivBytes = new byte[encryptCipher.getBlockSize()];
//      randomSecureRandom.nextBytes(ivBytes);
//      IvParameterSpec iv = new IvParameterSpec(ivBytes);
//      // Initiate the ciphers
//      encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
//      decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
//      // Send this key to the server
//      String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
//      String encodedIv = Base64.getEncoder().encodeToString(iv.getIV());
//      sendMessage(MsgType.CRYP + " " + encodedKey + " " + encodedIv);
//    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
//      e.printStackTrace();
//    }
//  }

  private static final String key = "aesEncryptionKey";
  private static final String initVector = "encryptionIntVec";
  private Cipher encryptCipher;
  private Cipher decryptCipher;

  private void initEncryption() {
    try {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
      encryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
      decryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
      sendMessage(MsgType.CRYP + " " + key + " " + initVector);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    }
  }

}
