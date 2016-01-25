package net.mineguild.ChatServer.client;

import java.io.Console;
import java.io.IOException;
import java.net.Socket;

public class Client {

    private Socket socket;

    private ConnectionInputProcess connection;
    private ConsoleProcess console;

    public Client(String host, int port) throws IOException{
        socket = new Socket(host, port);
    }

    public void start() throws IOException{
        console = new ConsoleProcess(System.in, socket);
        String name = console.askName();
        connection = new ConnectionInputProcess(socket, name);
        connection.start();
        console.start();

    }

    public void receiveMessage(String message){
        System.out.println(message);
    }

    public static void main(String[] args)throws IOException{
        Client c = new Client("localhost", 7785);
        c.start();
    }


}
