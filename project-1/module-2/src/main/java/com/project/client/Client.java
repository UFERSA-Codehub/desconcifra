package com.project.client;

public interface Client {
    void connect(String host, int port);

    void sendRequest(String serverName);

    void sendRegister(String serverName, String ipAddress);

    void start();

    void disconnect();
}