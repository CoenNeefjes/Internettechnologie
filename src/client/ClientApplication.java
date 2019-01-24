package client;

import client.service.MessageProcessor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import server.Server;

/**
 * Class that starts the client application
 *
 * @author Coen Neefjes
 */
public class ClientApplication {

  // Change this to the location you want files to be downloaded
  public static final String DOWNLOAD_LOCATION = "C:/Users/Coen Neefjes/IdeaProjects/Internettechnologie/src/files/received_";

  public static Set<String> clientNames = new HashSet<>();
  public static Set<String> groupNames = new HashSet<>();
  public static Set<String> subscribedGroups = new HashSet<>();
  public static Set<String> myGroups = new HashSet<>();

  public static void main(String[] args) {
    try {
      Socket socket = new Socket(InetAddress.getLocalHost(), Server.SERVER_PORT);
      System.out.println("Connected to server");

      Thread messageThread = new Thread(new MessageProcessor(socket));
      messageThread.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
