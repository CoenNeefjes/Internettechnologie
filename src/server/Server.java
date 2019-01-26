package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import server.model.Client;
import server.model.Group;
import server.service.MessageProcessor;

/**
 * Class that starts the server
 *
 * @author Coen Neefjes
 */
public class Server {

  public static final int SERVER_PORT = 1337;
  public static final int PING_INTERVAL = 60;

  public static CopyOnWriteArrayList<Client> clients = new CopyOnWriteArrayList<>();
  public static CopyOnWriteArrayList<Group> groups = new CopyOnWriteArrayList<>();

  public static void main(String[] args) {
    Server myServer = new Server();
    myServer.start();
  }

  private void start() {
    try {
      ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
      System.out.println("Server started on port " + SERVER_PORT);

      while (true) {
        // Accept connection
        Socket clientSocket = serverSocket.accept();
        System.out.println("Accepted client connection");

        // Set outputStream
        OutputStream outputStream = clientSocket.getOutputStream();

        // Send welcome message
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("HELO");
        writer.flush();

        // Start thread for message handling
        Thread messageThread = new Thread(new MessageProcessor(clientSocket));
        messageThread.start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets all the user names the server has saved
   * @return List of Strings containing all client names
   */
  public static List<String> getUserNames() {
    return clients.stream().map(Client::getName).collect(Collectors.toList());
  }

  /**
   * Gets all the group names the servers has saved
   * @return List of Strings containing all group names
   */
  public static List<String> getGroupNames() {
    return groups.stream().map(Group::getName).collect(Collectors.toList());
  }

  /**
   * Gets a group by its name
   * @param groupName The name of the group
   * @return The Group with the given name or null
   */
  public static Group getGroupByName(String groupName) {
    for (Group group : groups) {
      if (group.getName().equals(groupName)) {
        return group;
      }
    }
    return null;
  }

  /**
   * Gets a Client by its name
   * @param clientName The name of the Client
   * @return The Client with the given name or null
   */
  public static Client getClientByName(String clientName) {
    for (Client client : clients) {
      if (client.getName().equals(clientName)) {
        return client;
      }
    }
    return null;
  }

  /**
   * Removes the given Client from the Client List and from all Groups
   * @param client The Client that needs to be removed
   */
  public static void removeClient(Client client) {
    clients.remove(client);
    groups.forEach(group -> {
      group.removeGroupMember(client);
      if (group.getGroupMemberNames().size() == 0) {
        groups.remove(group);
      }
    });
  }
}
