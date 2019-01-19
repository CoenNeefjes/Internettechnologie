package general;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class CryptographyHandler {

  public static final String CIPHER_PROVIDER = "AES/CBC/PKCS5PADDING";

//  public static String encrypt(Cipher encryptCipher ,String plainText) {
//    try {
//      return new String(encryptCipher.doFinal(plainText.getBytes("UTF-8")));
//    } catch (BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException e) {
//      e.printStackTrace();
//    }
//    System.out.println("encryption went wrong");
//    return null;
//  }

  public static String encrypt(Cipher encryptCipher, String value) {
    try {
      byte[] encrypted = encryptCipher.doFinal(value.getBytes());
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

//  public static String decrypt(Cipher decryptCipher ,String encryptedMessage) {
//    System.out.println("decrypting: " + encryptedMessage);
//    try {
//      return new String(decryptCipher.doFinal(encryptedMessage.getBytes("UTF-8")));
//    } catch (BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException e) {
//      e.printStackTrace();
//    }
//    System.out.println("decryption went wrong");
//    return null;
//  }

  public static String decrypt(Cipher decryptCipher, String encrypted) {
    try {
      byte[] original = decryptCipher.doFinal(Base64.getDecoder().decode(encrypted));
      return new String(original);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

}
