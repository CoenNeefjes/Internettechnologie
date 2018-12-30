package server.service;

import general.MessageHandler;
import server.Server;
import server.model.Client;
import server.model.Group;
import server.util.ErrorMessageConstructor;
import server.util.MessageConstructor;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class MessageProcessor extends MessageHandler implements Runnable {

    private Client client;

    public MessageProcessor(Socket clientSocket) throws IOException {
        super(clientSocket);
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        while (socket.isConnected()) {
            receiveMessage(reader);
        }
    }

    private void sendMessage(String msg, PrintWriter writer) {
        writer.println(msg);
        writer.flush();
    }

    private void returnOkMessage(String line)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        sendMessage(MessageConstructor.okMessage(line), writer);
    }

    private void broadcastMessage(String line) throws IOException {
        for (Client client : Server.clients) {
            if (!client.getName().equals(this.client.getName())) {
                Socket socket = client.getClientSocket();
                sendMessage(MessageConstructor.broadcastMessage(this.client.getName(), line),
                        new PrintWriter(socket.getOutputStream()));
            }
        }
    }

    @Override
    protected void handleHelloMessage(String line) {
        try {
            if (client != null) {
                sendMessage(ErrorMessageConstructor.alreadyLoggedInError(), writer);
                socket.close();
                return;
            }

            String userName = line.substring(5);
            //TODO: check if username is valid string

            if (!Server.getUserNames().contains(userName)) {
                this.client = new Client(socket, userName);
                Server.clients.add(client);
                returnOkMessage(line);
                // Start heartbeat Thread
                Thread heartbeatThread = new Thread(new PingPong(socket));
                heartbeatThread.start();
            } else {
                sendMessage(ErrorMessageConstructor.alreadyLoggedInError(), writer);
                socket.close();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleBroadCastMessage(String line) {
        try {
            returnOkMessage(line);
            broadcastMessage(line.substring(5));
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleQuitMessage() {
        try {
            Server.clients.remove(client);
            sendMessage(MessageConstructor.quitMessage(), writer);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleClientListMessage(String line) {
        String clientListString = "";
        for (Client client : Server.clients) {
            clientListString += client.getName() + ", ";
        }
        sendMessage(MessageConstructor.clientListMessage(clientListString), writer);
    }

    @Override
    protected void handlePrivateMessage(String line) {
        String recipientName = line.split(" ")[0];
        System.out.println("HandlePrivateMessage for " + recipientName);

        // Try to get the client
        Client client = Server.getClientByName(recipientName);
        if (client != null) {
            try {
                // If client exists send message
                Socket socket = client.getClientSocket();
                sendMessage(
                        MessageConstructor
                                .privateMessage(this.client.getName(),
                                        line.substring(recipientName.length() + 1)),
                        new PrintWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendMessage(ErrorMessageConstructor.clientNotFoundError(), writer);
        }
    }

    @Override
    protected void handleCreateGroupMessage(String line) {
        //TODO: check if groupName is valid string
        String groupName = line.substring(5);
        try {
            if (!Server.getGroupNames().contains(groupName)) {
                Server.groups.add(new Group(groupName, client));
                returnOkMessage(line);
            } else {
                sendMessage(ErrorMessageConstructor.groupNameAlreadyExistsError(), writer);
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleGroupListMessage(String line) {
        String groupListString = "";
        for (Group group : Server.groups) {
            groupListString += group.getName() + ", ";
        }
        sendMessage(MessageConstructor.groupListMessage(groupListString), writer);
    }

    @Override
    protected void handleJoinGroupMessage(String groupName) {
        Group group = Server.getGroupByName(groupName);
        if (group != null) {
            group.addGroupMember(client);
        } else {
            sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
        }
    }

    @Override
    protected void handleGroupMessage(String line) {
        String groupName = line.split(" ")[0];
        String message = line.substring(groupName.length() + 1);

        System.out.println("HandleGroupMessage: groupName=" + groupName + ", message=" + message);

        // Try to get the group
        Group group = Server.getGroupByName(groupName);

        // If the group exists, proceed. Otherwise send error
        if (group != null) {
            // Check if this user is in the group
            if (group.getGroupMemberNames().contains(client.getName())) {
                // Get all clients from group except for this client
                group.getGroupMembers().stream()
                        .filter(groupMember -> !groupMember.getName().equals(client.getName()))
                        .forEach(groupMember -> {
                            try {
                                // Send the message to all group members
                                Socket socket = client.getClientSocket();
                                sendMessage(MessageConstructor.groupMessage(groupName, client.getName(), message),
                                        new PrintWriter(socket.getOutputStream()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            } else {
                sendMessage(ErrorMessageConstructor.userNotInGroupError(), writer);
            }
        } else {
            sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
        }
    }

    @Override
    protected void handleLeaveGroupMessage(String line) {
        String groupName = line.substring(5);
        System.out.println("handleLeaveGroupMessage: groupName=" + groupName);
        Group group = Server.getGroupByName(groupName);
        if (group != null) {
            // Check if user was in group
            if (group.getGroupMemberByName(client.getName()) != null) {
                // If the group owner leaves or the group becomes empty, destroy group
                if (group.isOwner(client) || group.getGroupMembers().isEmpty()) {
                    Server.groups.remove(group);
                }
                group.removeGroupMember(client);
                try {
                    returnOkMessage(line);
                } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                //TODO: let the group know the user disconnected
            } else {
                sendMessage(ErrorMessageConstructor.userNotInGroupError(), writer);
            }
        } else {
            sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
        }
    }

    @Override
    protected void handleKickGroupClientMessage(String line) {
        String groupName = line.split(" ")[0];
        String clientName = line.substring(groupName.length() + 1);

        System.out.println("handleKickGroupClientMessage: groupName=" + groupName + ", clientName=" + clientName);

        // Try to get the group
        Group group = Server.getGroupByName(groupName);
        if (group != null) {
            // Check if this user is group owner
            if (group.isOwner(client)) {
                // Check if the user that needs to be kicked is in the group
                Client client = group.getGroupMemberByName(clientName);
                if (client != null) {
                    // Kick the client
                    group.removeGroupMember(client);
                    //TODO: send a message to the client that he is removed
                    //TODO: send a message to the group the client is removed
                } else {
                    sendMessage(ErrorMessageConstructor.userNotInGroupError(), writer);
                }
            } else {
                sendMessage(ErrorMessageConstructor.notOwnerOfGroupError(), writer);
            }
        } else {
            sendMessage(ErrorMessageConstructor.groupNotFoundError(), writer);
        }
    }

    // Server should not receive PING messages
    @Override
    protected void handlePingMessage() {

    }

    @Override
    protected void handlePongMessage() {

    }
}
