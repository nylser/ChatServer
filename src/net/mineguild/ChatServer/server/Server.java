package net.mineguild.ChatServer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Sender {
    int port = 7785;
    private ServerSocket serverSocket;
    private List<ClientProcess> clients;

    public Server(){
        clients = new ArrayList<>();
    }

    public void openServer(){
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e){
            System.err.println("Unable open ServerSocket!"+e.getMessage());
        }
    }

    public void acceptClient(){
        try {
            Socket clientSocket = serverSocket.accept();
            ClientProcess process = new ClientProcess(clientSocket, this);
            clients.add(process);
            process.start();
        } catch (IOException e){
            System.err.println("Can't accept Client!"+e.getMessage());
        }
    }

    public synchronized void handleDisconnect(ClientProcess client){
        clients.remove(client);
    }

    public static void main(String[] args){
        Server server = new Server();
        server.openServer();
        while(true){
            server.acceptClient();
        }
    }

    public void broadcastMessage(Sender sender, String message) {
        clients.stream().forEach(client -> {
            client.sendMessage(sender.getSenderName() + "> " + message);
        });
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(">"+message);
    }

    @Override
    public String getSenderName() {
        return "Server";
    }
}
