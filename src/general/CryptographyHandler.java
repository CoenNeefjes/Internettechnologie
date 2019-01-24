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

/**
 * Class for encrypting and decrypting Strings
 *
 * @author Coen Neefjes
 */
public class CryptographyHandler {

  // This is the encoding algorithm/it's mode/the padding mode
  private final String CIPHER_PROVIDER = "AES/CBC/PKCS5PADDING";

  private Cipher encryptCipher;
  private Cipher decryptCipher;

  private String key;
  private String initVector;

  /**
   * Create a CryptographyHandler with random key and initVector
   */
  public CryptographyHandler() {
    // Generate key and initialisation vector
    RandomString randomStringGenerator = new RandomString();
    key = randomStringGenerator.nextString();
    initVector = randomStringGenerator.nextString();
    // Create the ciphers
    init();
  }

  /**
   * Create a CryptograohyHandler with a given key and initVector
   * @param key The key String for the algorithm
   * @param initVector The initVector String for the algorithm
   */
  public CryptographyHandler(String key, String initVector) {
    // Set the key and initVector
    this.key = key;
    this.initVector = initVector;
    // Create the ciphers
    init();
  }

  /**
   * Initialises the Cipher variables in this class using the already set key and initvector
   */
  private void init() {
    try {
      // Create the ciphers
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
      encryptCipher = Cipher.getInstance(CIPHER_PROVIDER);
      encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
      decryptCipher = Cipher.getInstance(CIPHER_PROVIDER);
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

  /**
   * Encrypts a plain text String
   * @param plainText The plain text String
   * @return An encrypted String
   */
  public String encrypt(String plainText) {
    try {
      byte[] encrypted = encryptCipher.doFinal(plainText.getBytes());
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Decrypts an encrypted String
   * @param encrypted The encrypted String
   * @return A decrypted String
   */
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
