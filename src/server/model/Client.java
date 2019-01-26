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

/**
 * Class that models a Client object
 */
public class Client {

  // Client variables
  private Socket clientSocket;
  private String name;

  // PING PONG
  private boolean receivedPong;

  // Encryption
  private CryptographyHandler cryptographyHandler;

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

  /**
   * Sets the receivedPong boolean to a new state
   * @param state true or false
   */
  public void setReceivedPong(boolean state) {
    this.receivedPong = state;
  }

  public boolean getReceivedPong() {
    return receivedPong;
  }

  public String getDecryptedMessage(String encryptedMessage) {
    return cryptographyHandler.decrypt(encryptedMessage);
  }

  public String encryptMessage(String plainText) {
    return cryptographyHandler.encrypt(plainText);
  }

  /**
   * Initialises the cryptographyHandler variable with the given key and vector
   * @param key The public key
   * @param initialisationVector The initialisation vector
   */
  public void initEncryption(String key, String initialisationVector) {
    cryptographyHandler = new CryptographyHandler(key, initialisationVector);
  }

}
