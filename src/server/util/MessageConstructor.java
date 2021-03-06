package server.util;

import general.MessageMD5Encoder;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Class with only static Strings representing all messages the server can respond with
 *
 * @author Coen Neefjes
 */
public class MessageConstructor {

  public static String okMessage(String message) {
    return "+OK " + MessageMD5Encoder.encode(message);
  }

  public static String broadcastMessage(String sender, String msg) {
    return "BCST " + sender + " " + msg;
  }

  public static String quitMessage() {
    return "QUIT";
  }

  public static String clientListMessage(String clientListString) {
    return "CLST " + clientListString.substring(0, clientListString.length() - 2);
  }

  public static String groupListMessage(String groupListString) {
    String result;
    if (groupListString.length() > 0) {
      result = "GLST " + groupListString.substring(0, groupListString.length() - 2);
    } else {
      result = "GLST";
    }
    return result;
  }

  public static String privateMessage(String sender, String msg) {
    return "PMSG " + sender + " " + msg;
  }

  public static String encryptedPrivateMessage(String encryptedMsg) {
    return "PMSG " + encryptedMsg;
  }

  public static String groupMessage(String groupName, String sender, String msg) {
    return "GMSG " + groupName + " " + sender + " " + msg;
  }

  public static String leaveGroupMessage(String groupName, String clientName) {
    return "LGRP " + groupName + " " + clientName;
  }

  public static String notifyGroupOfKickMessage(String groupName, String clientName) {
    return "KGCL " + groupName + " " + clientName;
  }

  public static String notifyClientOfKickMessage(String groupName) {
    return "KGCL " + groupName;
  }

  public static String fileMessage(String sender, String fileName, String fileString) {
    return "FILE " + sender + " " + fileName + " " + fileString;
  }

}
