package general;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Abstract class that handles incoming messages.
 * Should be implemented differently in client and server
 *
 * @author Coen Neefjes
 */
public abstract class MessageHandler {

  protected InputStream inputStream;
  protected Socket socket;
  protected PrintWriter writer;

  protected MessageHandler(Socket socket) throws IOException {
    this.socket = socket;
    this.inputStream = socket.getInputStream();
    this.writer = new PrintWriter(socket.getOutputStream());
  }

  /**
   * Checks what message has been received and sends it to the correct handle method
   * @param reader The BufferedReader that receives messages
   */
  protected void receiveMessage(BufferedReader reader) {
    try {
      String line = "";

      if (reader.ready()) {
        line = reader.readLine();
        System.out.println("Received message: " + line);
      }

      switch (line.split(" ")[0]) {
        case "HELO":
          handleHelloMessage(line);
          break;
        case "QUIT":
          handleQuitMessage();
          break;
        case "BCST":
          handleBroadCastMessage(line);
          break;
        case "CLST":
          handleClientListMessage(line);
          break;
        case "PMSG":
          handlePrivateMessage(line);
          break;
        case "CGRP":
          handleCreateGroupMessage(line);
          break;
        case "GLST":
          handleGroupListMessage(line);
          break;
        case "JGRP":
          handleJoinGroupMessage(line);
          break;
        case "GMSG":
          handleGroupMessage(line);
          break;
        case "LGRP":
          handleLeaveGroupMessage(line);
          break;
        case "KGCL":
          handleKickGroupClientMessage(line);
          break;
        case "PING":
          handlePingMessage();
          break;
        case "PONG":
          handlePongMessage();
          break;
        case "CRYP":
          handleCryptoKeyMessage(line);
          break;
        case "FILE":
          handleFileMessage(line);
          break;
        case "-ERR":
          handleErrorMessage(line.substring(5));
          break;
        case "+OK":
          handleOkMessage(line.substring(4));
          break;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected abstract void handleHelloMessage(String line);

  protected abstract void handleQuitMessage() throws IOException;

  protected abstract void handleBroadCastMessage(String line);

  protected abstract void handleClientListMessage(String line);

  protected abstract void handlePrivateMessage(String line);

  protected abstract void handleCreateGroupMessage(String line);

  protected abstract void handleGroupListMessage(String line);

  protected abstract void handleJoinGroupMessage(String line);

  protected abstract void handleGroupMessage(String line);

  protected abstract void handleLeaveGroupMessage(String line);

  protected abstract void handleKickGroupClientMessage(String line);

  protected abstract void handlePingMessage();

  protected abstract void handlePongMessage();

  protected abstract void handleErrorMessage(String line);

  protected abstract void handleOkMessage(String line);

  protected abstract void handleCryptoKeyMessage(String line);

  protected abstract void handleFileMessage(String line);

}
