package server.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import server.Server;
import server.model.Client;

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
        // We haven't received the PONG yet
        Server.receivedPongPerClient.put(client.getName(), false);

        // Send PING message
        writer.println("PING");
        writer.flush();

        // Wait 3 seconds
        Thread.sleep(3000);

        // Check if we received the PONG message
        if (!Server.receivedPongPerClient.get(client.getName())) {
          System.out.println(client.getName() + " has not sent a PONG in time, terminating connection");
          Server.clients.remove(client);
          Server.receivedPongPerClient.remove(client.getName());
          clientSocket.close();
        }

        // Wait a minute before resending the PING message
        Thread.sleep(5000);
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
