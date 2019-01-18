package client;

import client.service.MessageProcessor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientApplication {

  //TODO: stop the application after quit message, probably still waiting for printer.readLine or printer.ready

  //TODO: encryption: https://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/samples/index.html

  public static Set<String> clientNames = new HashSet<>();
  public static Set<String> groupNames = new HashSet<>();
  public static Set<String> subscribedGroups = new HashSet<>();
  public static Set<String> myGroups = new HashSet<>();

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
