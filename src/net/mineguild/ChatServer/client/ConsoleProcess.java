package net.mineguild.ChatServer.client;

import java.io.*;
import java.net.Socket;

public class ConsoleProcess extends Thread{

    private BufferedReader reader;
    private PrintWriter output;

    public ConsoleProcess(InputStream input, Socket s)throws IOException{
        output = new PrintWriter(s.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(input));
    }

    @Override
    public void run(){
        while(!isInterrupted()){
            try {
                String input = reader.readLine();
                output.println(input);
                output.flush();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public String askName(){
        try {
            System.out.print("Name: ");
            String name = reader.readLine();
            if(name.isEmpty()){
                System.out.println("Name cannot be empty!");
                return askName();
            }
            return name;
        } catch (IOException e){
            System.err.println("Couldn't read name!");
            return askName();
        }
    }

}
