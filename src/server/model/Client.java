package server.model;

import general.CryptographyHandler;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Client {

  private Socket clientSocket;
  private String name;
  private SecretKey secretKey;
  private IvParameterSpec iv;
  private Cipher encryptCipher;
  private Cipher decryptCipher;

  public Client(Socket clientSocket, String name) {
    this.clientSocket = clientSocket;
    this.name = name;
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

  public String getName() {
    return name;
  }

  public void setSecretKey(SecretKey secretKey) {
    this.secretKey = secretKey;
  }

  public void setInitialisationVector(byte[] iv) {
    this.iv = new IvParameterSpec(iv);
  }

  public String getDecryptedMessage(String encryptedMessage) {
    return CryptographyHandler.decrypt(decryptCipher, encryptedMessage);
  }

  public String encrypMessage(String plainText) {
    return CryptographyHandler.encrypt(encryptCipher, plainText);
  }

  public void initEncryption() {
    try {
      encryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
      decryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
      e.printStackTrace();
    }
  }

}
