package client.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class InputSender implements Runnable {

    private Socket socket;
    private PrintWriter writer;
    private Scanner scanner;

    public InputSender(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream());
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            sendMessage(scanner.nextLine());
        }
    }

    private void sendMessage(String msg) {
        writer.println(msg);
        writer.flush();
    }
}
