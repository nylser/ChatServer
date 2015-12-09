package net.mineguild.ChatServer.server;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientProcess extends Thread implements Sender {
    private Socket socket;
    private Server server;
    private String name = "";
    private boolean isEncrypted;

    private BufferedReader input;
    private PrintWriter output;

    public ClientProcess(Socket socket, Server server, String name)throws IOException{
        this.socket = socket;
        this.server = server;
        this.name = name;
        isEncrypted = socket instanceof SSLSocket;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream());
    }


    @Override
    public void run(){
        try {
            System.out.println(name+ " connected");
            server.broadcastMessage(server, name+ " connected!");
            String message;
            while (socket.isConnected() && (message = input.readLine()) != null) {
                if(message.startsWith("/")){
                    if(message.equals("/bye")) {
                        socket.close();
                    } else if (message.equals("/list")){
                        StringBuilder builder = new StringBuilder("Clients: ");
                        List<String> clientNames = server.getClientNames();
                        for (int i = 0; i < clientNames.size() ; i++) {
                            builder.append(clientNames.get(i));
                            if(i+1 != clientNames.size()) {
                                builder.append(", ");
                            }
                        }
                        sendMessage(builder.toString());
                    }
                } else {
                    server.broadcastMessage(this, message);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(name+" disconnected");
        server.broadcastMessage(server, name+ " disconnected!");
        server.handleDisconnect(this);
    }



    @Override
    public void sendMessage(String message) {
        output.println(message);
        output.flush();
    }

    @Override
    public String getSenderName(){
        return this.name;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }
}
