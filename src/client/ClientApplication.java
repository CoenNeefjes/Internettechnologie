package client;

import client.gui.ClientGui;
import client.service.MessageProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientApplication {

    public static void main(String[] args) {
        ClientGui clientGui = new ClientGui();
        clientGui.setVisible(true);
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), 1337);
            System.out.println("Connected to server");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            Thread messageThread = new Thread(new MessageProcessor(socket));
            messageThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
