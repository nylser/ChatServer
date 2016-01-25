package net.mineguild.ChatServer.server;

public interface Sender {
    void sendMessage(String message);
    String getSenderName();
}
