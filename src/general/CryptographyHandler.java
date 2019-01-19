package general;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class CryptographyHandler {

  public static String encrypt(Cipher encryptCipher ,String plainText) {
    try {
      return new String(encryptCipher.doFinal(plainText.getBytes()));
    } catch (BadPaddingException | IllegalBlockSizeException e) {
      e.printStackTrace();
    }
    System.out.println("encryption went wrong");
    return null;
  }

  public static String decrypt(Cipher decryptCipher ,String encryptedMessage) {
    try {
      return new String(decryptCipher.doFinal(encryptedMessage.getBytes()));
    } catch (BadPaddingException | IllegalBlockSizeException e) {
      e.printStackTrace();
    }
    System.out.println("decryption went wrong");
    return null;
  }

}
