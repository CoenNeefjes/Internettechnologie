package general;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class MessageMD5Encoder {

  public static String encode(String message) {
    try {
      byte[] bytesOfMessage = message.getBytes("UTF-8");
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] encoded = Base64.getEncoder().encode(md.digest(bytesOfMessage));

      return new String(encoded);

    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

}
