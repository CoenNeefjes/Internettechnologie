package server.model;

import java.net.Socket;

public class Client {

  private Socket clientSocket;
  private String name;

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

}
