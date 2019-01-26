package server.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import server.Server;
import server.model.Client;

/**
 * Class that checks if a client is still connected
 *
 * @author Coen Neefjes
 */
public class PingPong implements Runnable {

  private OutputStream outputStream;

  private Client client;
  private Socket clientSocket;

  public PingPong(Client client) throws IOException {
    this.client = client;
    this.clientSocket = client.getClientSocket();
    this.outputStream = clientSocket.getOutputStream();
  }

  @Override
  public void run() {
    // Start read en write
    PrintWriter writer = new PrintWriter(outputStream);

    while (clientSocket.isConnected() && !clientSocket.isClosed()) {
      try {
        System.out.println("sending ping");
        // We haven't received the PONG yet
        client.setReceivedPong(false);

        // Send PING message
        writer.println("PING");
        writer.flush();

        // Wait 3 seconds
        Thread.sleep(3000);

        // Check if we received the PONG message
        if (!client.getReceivedPong()) {
          System.out.println(client.getName() + " sent no PONG, terminating connection");
          Server.removeClient(client);
          clientSocket.close();
        }

        // Wait a minute before resending the PING message
        Thread.sleep(Server.PING_INTERVAL * 1000);
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
