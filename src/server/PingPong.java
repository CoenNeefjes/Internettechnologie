package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class PingPong implements Runnable {

  private InputStream inputStream;
  private OutputStream outputStream;

  private Socket clientSocket;

  public PingPong(Socket clientSocket) throws IOException {
    this.inputStream = clientSocket.getInputStream();
    this.outputStream = clientSocket.getOutputStream();
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    while (clientSocket.isConnected()) {
      try {
        // Start read en write
        PrintWriter writer = new PrintWriter(outputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        writer.println("PING");
        writer.flush();

        // Wait 3 seconds before checking for PONG message
        Thread.sleep(3000);

        if (reader.ready()) {
          String line = reader.readLine();
          if (!line.startsWith("PONG")) {
            System.out.println("No PONG message received");
            //TODO: remove client and username from server
            clientSocket.close();
          }
        }

        // Wait a minute before resending the PING message
        Thread.sleep(60000);
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
