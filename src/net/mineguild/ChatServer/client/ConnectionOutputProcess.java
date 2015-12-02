package net.mineguild.ChatServer.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionOutputProcess extends Thread {

    private Socket socket;
    private PrintWriter output;


    public ConnectionOutputProcess(Socket socket) throws IOException {
        this.socket = socket;
        output = new PrintWriter(socket.getOutputStream());
    }

    public void sendMessage(String message){
        output.println(message);
    }

}
