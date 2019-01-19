package server.model;

import general.CryptographyHandler;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Client {

  private Socket clientSocket;
  private String name;
//  private SecretKey secretKey;
//  private IvParameterSpec iv;
  private Cipher encryptCipher;
  private Cipher decryptCipher;

  private String key;
  private String iv;

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

//  public void setSecretKey(SecretKey secretKey) {
//    this.secretKey = secretKey;
//  }
//
//  public void setInitialisationVector(byte[] iv) {
//    this.iv = new IvParameterSpec(iv);
//  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setIv(String iv) {
    this.iv = iv;
  }

  public String getDecryptedMessage(String encryptedMessage) {
    System.out.println("encrypted serverSide: " + encryptMessage("henk hey"));
    return CryptographyHandler.decrypt(decryptCipher, encryptedMessage);
  }

  public String encryptMessage(String plainText) {
    return CryptographyHandler.encrypt(encryptCipher, plainText);
  }

//  public void initEncryption() {
//    try {
//      encryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
//      encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
//      decryptCipher = Cipher.getInstance(CryptographyHandler.CIPHER_PROVIDER);
//      decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
//    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
//      e.printStackTrace();
//    }
//  }

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
