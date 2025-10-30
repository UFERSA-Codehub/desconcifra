package com.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.project.crypto.Message;
import com.project.crypto.MessageType;

public class DiscoveryClientHandler extends Thread {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Discovery server;
    public String name;

    public DiscoveryClientHandler(Socket socket, Discovery server) {
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

    private void handleCalculate(Message message) {
        String serviceName = "CalculatorService";
        
        ServiceInfo selectedServer = server.selectServer(serviceName, "round-robin");

        if (selectedServer == null) {
            Message response = new Message(MessageType.ERROR, serviceName, "NO_SERVERS_AVAILABLE");
            output.println(response.pack());
            System.out.printf("Nenhum servidor disponível para: %s solicitado por %s%n", 
                serviceName, socket.getInetAddress());
            return;
        }

        Message redirect = Message.createRedirectMessage(
            selectedServer.getServerName(), 
            selectedServer.getHost(), 
            selectedServer.getPort()
        );
        output.println(redirect.pack());
        System.out.printf("REDIRECT enviado para %s: %s -> %s:%d%n", 
            socket.getInetAddress(), 
            selectedServer.getServerName(),
            selectedServer.getHost(),
            selectedServer.getPort());
    }

    private void handleRegister(Message message) {
        String serviceName = message.getServerName();
        String addressInfo = message.getIpAddress();

        String[] parts = addressInfo.split(":");
        if (parts.length != 3) {
            Message response = new Message(MessageType.ERROR, serviceName, "INVALID_FORMAT");
            output.println(response.pack());
            return;
        }

        String serverName = parts[0];
        String host = parts[1];
        int port = Integer.parseInt(parts[2]);

        server.registerService(serviceName, serverName, host, port);

        Message response = new Message(MessageType.RESPONSE, serviceName, "REGISTERED");
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

                if (message.getType() == MessageType.CALCULATE) {
                    handleCalculate(message);
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
