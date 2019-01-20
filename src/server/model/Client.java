package server.model;

import general.CryptographyHandler;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Client {

  // Client variables
  private Socket clientSocket;
  private String name;

  // PING PONG
  private boolean receivedPong;

  // Encryption variables
  private Cipher encryptCipher;
  private Cipher decryptCipher;
  private String key;
  private String iv;

  public Client(Socket clientSocket, String name) {
    this.clientSocket = clientSocket;
    this.name = name;
    this.receivedPong = false;
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

  public String getName() {
    return name;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setIv(String iv) {
    this.iv = iv;
  }

  public void setReceivedPong(boolean state) {
    this.receivedPong = state;
  }

  public boolean getReceivedPong() {
    return receivedPong;
  }

  public String getDecryptedMessage(String encryptedMessage) {
    return CryptographyHandler.decrypt(decryptCipher, encryptedMessage);
  }

  public String encryptMessage(String plainText) {
    return CryptographyHandler.encrypt(encryptCipher, plainText);
  }

  public void initEncryption() {
    try {
      IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("UTF-8"));
      SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
      encryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
      decryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
      decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
    } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

}
