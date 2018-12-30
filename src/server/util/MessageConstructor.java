package server.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class MessageConstructor {

    public static String okMessage(String msg)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] bytesOfMessage = msg.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] encoded = Base64.getEncoder().encode(md.digest(bytesOfMessage));
        return "+OK " + new String(encoded);
    }

    public static String broadcastMessage(String sender, String msg) {
        return "BCST " + sender + " " + msg;
    }

    public static String quitMessage() {
        return "+OK Goodbye";
    }

    public static String clientListMessage(String clientListString) {
        return "CLST " + clientListString.substring(0, clientListString.length() - 2);
    }

    public static String groupListMessage(String groupListString) {
        String result;
        if (groupListString.length() > 0) {
            result = "GLST " + groupListString.substring(0, groupListString.length() - 2);
        } else {
            result = "GLST no groups were found";
        }
        return result;
    }

    public static String privateMessage(String sender, String msg) {
        return "PMSG " + sender + " " + msg;
    }

    public static String groupMessage(String groupName, String sender, String msg) {
        return "GMSG " + groupName + " " + sender + " " + msg;
    }

}
