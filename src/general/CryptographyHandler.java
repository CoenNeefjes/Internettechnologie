package general;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptographyHandler {

  public static final String CIPHER_PROVIDER = "AES/CBC/PKCS5PADDING";

  private Cipher encryptCipher;
  private Cipher decryptCipher;

  private String key;
  private String initVector;

  public CryptographyHandler() {
    // Generate key and initialisation vector
    RandomString randomStringGenerator = new RandomString();
    key = randomStringGenerator.nextString();
    initVector = randomStringGenerator.nextString();
    // Create the ciphers
    init();
  }

  public CryptographyHandler(String key, String initVector) {
    // Set the key and initVector
    this.key = key;
    this.initVector = initVector;
    // Create the ciphers
    init();
  }

  private void init() {
    try {
      // Create the ciphers
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
      encryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
      decryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
    } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public String getKey() {
    return key;
  }

  public String getInitVector() {
    return initVector;
  }

  public String encrypt(String value) {
    try {
      byte[] encrypted = encryptCipher.doFinal(value.getBytes());
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public String decrypt(String encrypted) {
    try {
      byte[] original = decryptCipher.doFinal(Base64.getDecoder().decode(encrypted));
      return new String(original);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

}
