package net.mineguild.ChatServer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientProcess extends Thread implements Sender {
    private Socket socket;
    private Server server;
    private String name = "";

    private BufferedReader input;
    private PrintWriter output;

    public ClientProcess(Socket socket, Server server)throws IOException{
        this.socket = socket;
        this.server = server;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream());
    }


    @Override
    public void run(){
        try {
            name = input.readLine();
            System.out.println(name+ " connected");
            server.broadcastMessage(server, name+ " connected!");
            String message;
            while (socket.isConnected() && (message = input.readLine()) != null) {
                server.broadcastMessage(this, message);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
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
}
