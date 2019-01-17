package client;

import client.service.MessageProcessor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import server.model.Client;

public class ClientApplication {

  public static Set<String> clientNames = new HashSet<>();
  public static Set<String> groupNames = new HashSet<>();

  public static void main(String[] args) {
    try {
      Socket socket = new Socket(InetAddress.getLocalHost(), 1337);
      System.out.println("Connected to server");

      Thread messageThread = new Thread(new MessageProcessor(socket));
      messageThread.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
