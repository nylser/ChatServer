package net.mineguild.ChatServer.server;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Sender {
    int port = 7785;
    private final Map<String, ClientProcess> clients;

    public Server() {
        clients = new HashMap<>();
    }

    public void serverLoop(ServerSocket serverSocket){
        while(true){
            try {
                Socket clientSocket = serverSocket.accept();
                Optional<String> name = negotiateName(clientSocket);
                if (name.isPresent()) {
                    startClient(clientSocket, name.get());
                } else {
                    clientSocket.close();
                }
            } catch (IOException e){
                e.printStackTrace();
                System.err.println("Error in Server Thread!");
            }
        }
    }

    private synchronized void startClient(Socket clientSocket, String name) throws IOException {
        ClientProcess process = new ClientProcess(clientSocket, this, name);
        clients.put(name, process);
        process.start();
    }

    public synchronized void handleDisconnect(ClientProcess client) {
        clients.remove(client.getSenderName());
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        Thread normal = new Thread(() -> {
            try {
                ServerSocket socket = new ServerSocket(7785);
                server.serverLoop(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread encrypted = new Thread(()-> {
          try{
              System.setProperty("javax.net.ssl.keyStore", "MGKey.store");
              System.setProperty("javax.net.ssl.keyStorePassword", "password");
              ServerSocket serverSocket;
              if(System.getProperties().containsKey("javax.net.ssl.keyStore") && System.getProperties().containsKey("javax.net.ssl.keyStorePassword")) {
                  SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                  serverSocket = factory.createServerSocket(7885);
                  server.serverLoop(serverSocket);
              } else {
                  System.out.println("Ignoring SSL-Server. Key-Properties not found.");
              }
          } catch (IOException e){
              e.printStackTrace();
          }
        });
        normal.start();
        encrypted.start();
    }

    public void broadcastMessage(Sender sender, String message) {
        if(sender instanceof ClientProcess && ((ClientProcess) sender).isEncrypted()){
            clients.values().stream().filter(clientProcess -> clientProcess.isEncrypted()).forEach(client -> {
                client.sendMessage(sender.getSenderName() + "> " + message);
            });
            clients.values().stream().filter(client -> !client.isEncrypted()).forEach(client -> {
                client.sendMessage(sender.getSenderName() + " sent an encrypted message! You have to be on a encrypted connection to see them!");
            });
        }
        else {
            clients.values().stream().forEach(client -> {
                client.sendMessage(sender.getSenderName() + "> " + message);
            });
        }
    }

    public synchronized boolean isNameUsed(String name) {
        return clients.containsKey(name);
    }

    public Optional<String> negotiateName(Socket clientSocket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String name = input.readLine();
        if(!isNameUsed(name)){
            clientSocket.getOutputStream().write("ok\n".getBytes());
            return Optional.of(name);
        } else {
            clientSocket.getOutputStream().write("name_used\n".getBytes());
            return Optional.empty();
        }
    }

    public List<String> getClientNames() {
        return new ArrayList<>(clients.keySet());
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(">" + message);
    }

    @Override
    public String getSenderName() {
        return "Server";
    }
}
