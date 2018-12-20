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
        System.out.println("Enter username: ");
        sendMessage("HELO " + scanner.nextLine());
    }

    @Override
    protected void handleQuitMessage() throws IOException {

    }

    @Override
    protected void handleBroadCastMessage(String line) {

    }

    @Override
    protected void handleClientListMessage() {

    }

    @Override
    protected void handlePrivateMessage(String line) {

    }

    @Override
    protected void handleCreateGroupMessage(String line) {

    }

    @Override
    protected void handleGroupListMessage() {

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

    }

    // Client should not receive PONG messages
    @Override
    protected void handlePongMessage() {

    }
}
