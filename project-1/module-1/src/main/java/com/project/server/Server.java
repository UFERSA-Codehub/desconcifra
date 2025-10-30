package com.project.server;

public interface Server {
    String lookup(String serverName);

    void updateEntry(String serverName, String newIP);

    void broadcast(String serverName, String newIP);

    void addRequestClient(ClientHandler clientHandler);

    void removeRequestClient(ClientHandler clientHandler);

    void start();

    void shutdown();
}
