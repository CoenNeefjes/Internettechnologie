package server;

import server.model.Client;
import server.model.Group;
import server.util.StringValidator;

public class test {

  public static void main(String[] args) {
    Client client1 = new Client(null, "Client1");
    Client client2 = new Client(null, "Client2");

    Group group = new Group("Group1", client1);
    System.out.println(group.isOwner(client1));

    System.out.println(StringValidator.validateString("Coen neefjes6"));
  }
}
