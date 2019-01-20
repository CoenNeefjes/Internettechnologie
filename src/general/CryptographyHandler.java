package general;

import java.util.Base64;
import javax.crypto.Cipher;

public class CryptographyHandler {

  public static final String CIPHER_PROVIDER = "AES/CBC/PKCS5PADDING";

  public static String encrypt(Cipher encryptCipher, String value) {
    try {
      byte[] encrypted = encryptCipher.doFinal(value.getBytes());
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

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
