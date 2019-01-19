package server.model;

import general.CryptographyHandler;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Client {

  private Socket clientSocket;
  private String name;
  private SecretKey secretKey;
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
    initEncryption();
  }

  public String getDecryptedMessage(String encryptedMessage) {
    return CryptographyHandler.decrypt(decryptCipher, encryptedMessage);
  }

  public String encrypMessage(String plainText) {
    return CryptographyHandler.encrypt(encryptCipher, plainText);
  }

  private void initEncryption() {
    try {
      encryptCipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
      encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
      decryptCipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
      decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
      e.printStackTrace();
    }
  }

}
