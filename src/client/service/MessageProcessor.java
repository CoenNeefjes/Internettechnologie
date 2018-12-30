package client.service;

import general.MessageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class MessageProcessor extends MessageHandler implements Runnable {

    private Scanner scanner;

    public MessageProcessor(Socket serverSocket) throws IOException {
        super(serverSocket);
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        while (socket.isConnected()) {
            receiveMessage(reader);
        }
    }

    private void sendMessage(String msg) {
        writer.println(msg);
        writer.flush();
    }

    @Override
    protected void handleHelloMessage(String line) {
        try {
            System.out.println("Enter username: ");
            sendMessage("HELO " + scanner.nextLine());
            Thread inputSenderThread = new Thread(new InputSender(socket));
            inputSenderThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Client should not receive quit message
    @Override
    protected void handleQuitMessage() {

    }

    @Override
    protected void handleBroadCastMessage(String line) {
        System.out.println(line);
    }

    @Override
    protected void handleClientListMessage(String line) {
        System.out.println("Online clients: " + line.substring(5));
    }

    @Override
    protected void handlePrivateMessage(String line) {
        String name = line.split(" ")[0];
        System.out.println("<private message> <from: " + name + "> " + line.substring(name.length()+1));
    }

    @Override
    protected void handleCreateGroupMessage(String line) {

    }

    @Override
    protected void handleGroupListMessage(String line) {
        System.out.println("Current groups: " + line.substring(5));
    }

    @Override
    protected void handleJoinGroupMessage(String line) {

    }

    @Override
    protected void handleGroupMessage(String line) {

    }

    @Override
    protected void handleLeaveGroupMessage(String line) {

    }

    @Override
    protected void handleKickGroupClientMessage(String line) {

    }

    @Override
    protected void handlePingMessage() {
        sendMessage("PONG");
    }

    // Client should not receive PONG messages
    @Override
    protected void handlePongMessage() {

    }
}
