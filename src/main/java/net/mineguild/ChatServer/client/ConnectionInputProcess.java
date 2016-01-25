package net.mineguild.ChatServer.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionInputProcess extends Thread {
    private PrintWriter output;
    private BufferedReader input;
    private Socket socket;
    private String name;

    public ConnectionInputProcess(Socket socket, String name) throws IOException{
        this.socket = socket;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream());
        this.name = name;
    }

    @Override
    public void run(){
        try{
            output.println(name);
            output.flush();
            String message;
            while(socket.isConnected() && (message = input.readLine()) != null){
                System.out.println(message);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
