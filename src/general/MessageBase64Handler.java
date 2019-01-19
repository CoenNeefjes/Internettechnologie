package general;


import java.util.Base64;

public class MessageBase64Handler {

  public static String encode(String toEncode) {
    return new String(Base64.getEncoder().encode(toEncode.getBytes()));
  }

  public static String decode(String encoded) {
    return new String(Base64.getDecoder().decode(encoded.getBytes()));
  }

}
