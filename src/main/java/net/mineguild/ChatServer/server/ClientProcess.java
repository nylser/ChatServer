package net.mineguild.ChatServer.server;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientProcess extends Thread implements Sender {
    private Socket socket;
    private Server server;
    private String name = "";
    private boolean isEncrypted;


    // Suspension
    private boolean isSuspended;
    private String suspendedID;
    private List<String> suspendedMessages = new ArrayList<>();


    private BufferedReader input;
    private PrintWriter output;

    public ClientProcess(Socket socket, Server server, String name) throws IOException {
        this.socket = socket;
        this.server = server;
        this.name = name;
        isEncrypted = socket instanceof SSLSocket;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream());
    }


    @Override
    public void run() {
        try {
            System.out.println(name + " connected");
            server.broadcastMessage(server, name + " connected!");
            String message;
            isSuspended = false;
            while (socket.isConnected() && (message = input.readLine()) != null) {
                if(!suspendedMessages.isEmpty()){
                    sendMessage("Missed messages");
                    suspendedMessages.forEach(this::sendMessage);
                }
                if (message.startsWith("/")) {
                    if (message.equals("/bye")) {
                        socket.close();
                    } else if (message.equals("/list")) {
                        StringBuilder builder = new StringBuilder("Clients: ");
                        List<String> clientNames = server.getClientNames();
                        for (int i = 0; i < clientNames.size(); i++) {
                            builder.append(clientNames.get(i));
                            if (i + 1 != clientNames.size()) {
                                builder.append(", ");
                            }
                        }
                        sendMessage(builder.toString());
                    } else if (message.startsWith("/msg")) {
                        try {
                            String name = message.split(" ")[1];
                            if (server.isNameUsed(name)) {
                                server.broadcastMessage(this, message.split(" ", 3)[2], name);
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            sendMessage("Invalid command syntax! /msg [name] [message]");
                        }
                    } else if (message.startsWith("/suspend")){
                        if(message.split(" ").length == 2){
                            String[] split = message.split(" ");
                            String id = split[1];
                            this.suspendedID = id; // TODO: Check that suspended id is unique
                            isSuspended = true;
                            socket.close(); // TODO: Send success message to client
                        }
                    }
                } else {
                    server.broadcastMessage(this, message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(name + " disconnected");
        server.broadcastMessage(server, name + " disconnected!");
        server.handleDisconnect(this);
    }

    public boolean getSuspended() {
        return isSuspended;
    }

    public String getSuspendedID() {
        return suspendedID;
    }

    public void refreshSocket(Socket newSocket){
        this.socket = newSocket;
    }

    @Override
    public void sendMessage(String message) {
        if (!isSuspended) {
            output.println(message);
            output.flush();
        } else {
            suspendedMessages.add(message);
        }
    }

    @Override
    public String getSenderName() {
        return this.name;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }
}
