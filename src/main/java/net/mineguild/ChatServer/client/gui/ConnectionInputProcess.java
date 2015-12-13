package net.mineguild.ChatServer.client.gui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionInputProcess extends Thread {
    private PrintWriter output;
    private BufferedReader input;
    private Socket socket;
    private TextArea textArea;
    private String name;
    private boolean alive;

    public ConnectionInputProcess(Socket socket, String name, TextArea textArea) throws IOException {
        this.socket = socket;
        this.textArea = textArea;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream());
        this.name = name;
    }

    @Override
    public void run() {
        try {
            output.println(name);
            output.flush();
            if(input.readLine().equals("name_used")){
                System.out.println("Name is already in use!");
                socket.close();
                Platform.exit();
                return;
            } else {
                System.out.println("Client connected!");
            }
            String message;
            while ((message = input.readLine()) != null) {
                final String shownMessage = message + "\n";
                try {
                    Platform.runLater(() -> {
                        textArea.appendText(shownMessage);
                    });
                } catch (NullPointerException e2) {
                    e2.printStackTrace();
                }
                System.out.println("Got message:" + message);
            }
        } catch (SocketException e){
            if(e.getMessage().equals("Socket closed")){
                System.out.println("Socket was closed.");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
