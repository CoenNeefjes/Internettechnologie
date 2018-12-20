package server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Group {

  private String name;
  private List<Client> groupMembers;
  private Client owner;

  public Group(String name, Client owner) {
    this.name = name;
    this.owner = owner;
    this.groupMembers = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public List<Client> getGroupMembers() {
    return groupMembers;
  }

  public List<String> getGroupMemberNames() {
    return groupMembers.stream().map(Client::getName).collect(Collectors.toList());
  }

  public Client getGroupMemberByName(String name) {
    for (Client client : groupMembers) {
      if (client.getName().equals(name)) {
        return client;
      }
    }
    return null;
  }

  public void addGroupMember(Client client) {
    groupMembers.add(client);
  }

  public void removeGroupMember(Client client) {
    groupMembers.remove(client);
  }

  public boolean isOwner(Client client) {
    return client.equals(owner);
  }
}
