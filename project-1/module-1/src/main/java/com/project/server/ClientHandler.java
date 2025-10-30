package com.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.project.crypto.Message;
import com.project.crypto.MessageType;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Server server;
    private boolean isClient = false;
    public String name;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.name = socket.getInetAddress().toString() + ":" + socket.getPort();
        try {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Erro ao configurar streams de E/S: " + e.getMessage());
        }
    }

    public void sendUpdate(String serverName, String newIP) {
        Message update = new Message(MessageType.UPDATE, serverName, newIP);
        output.println(update.pack());
    }

    private void handleRequest(Message message) {
        String serverName = message.getServerName();
        String ipAddress = server.lookup(serverName);

        if (ipAddress == null) {
            Message response = new Message(MessageType.RESPONSE, serverName, "NOT_FOUND");

            System.out.println("Mensagem enviada: " + response.serialize());
            output.println(response.pack());

            System.out.printf("Nome de servidor não encontrado para %s solicitado por %s%n", serverName,
                    socket.getInetAddress());
            return;
        }

        Message response = new Message(MessageType.RESPONSE, serverName, ipAddress);
        output.println(response.pack());
        System.out.printf("Resposta enviada para %s: %s -> %s%n", socket.getInetAddress(), serverName,
                ipAddress);

    }

    private void handleRegister(Message message) {
        String serverName = message.getServerName();
        String ipAddress = message.getIpAddress();

        server.updateEntry(serverName, ipAddress);

        Message response = new Message(MessageType.RESPONSE, serverName, ipAddress);
        output.println(response.pack());
    }

    @Override
    public void run() {
        try {
            String PackedMessage;
            while ((PackedMessage = input.readLine()) != null) {
                Message message = Message.unpack(PackedMessage);

                if (message == null) {
                    System.out.println("ERRO: Mensagem inválida (Verificação HMAC falhou)");
                    continue;
                }

                System.out.println("Mensagem recebida: " + message.serialize());

                if (message.getType() == MessageType.REQUEST) {
                    isClient = true;
                    server.addRequestClient(this);
                    handleRequest(message);
                } else if (message.getType() == MessageType.REGISTER) {
                    handleRegister(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        if (isClient) {
            server.removeRequestClient(this);
        }
        try {
            if (input != null)
                input.close();
            if (output != null)
                output.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexões: " + e.getMessage());
        }
    }
}