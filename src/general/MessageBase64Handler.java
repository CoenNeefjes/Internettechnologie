package general;


import java.util.Base64;

/**
 * Class that encodes and decodes Base64 Strings
 *
 * @author Coen Neefjes
 */
public class MessageBase64Handler {

  /**
   * Encodes a String using Base64
   * @param toEncode The String that needs to be encoded
   * @return A Base64 encoded String
   */
  public static String encode(String toEncode) {
    return new String(Base64.getEncoder().encode(toEncode.getBytes()));
  }

  /**
   * Decodes a Base64 String
   * @param encoded The Base64 String
   * @return Decoded String
   */
  public static String decode(String encoded) {
    return new String(Base64.getDecoder().decode(encoded.getBytes()));
  }

}
