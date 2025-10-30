package com.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.project.crypto.DebugConfig;
import com.project.crypto.Message;
import com.project.crypto.MessageType;

public class CalculatorImpl implements Calculator {
    private String serverName;
    private int port;
    private String host;
    private ServerSocket serverSocket;
    private boolean running;

    public CalculatorImpl(String serverName, String host, int port) throws IOException {
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.running = false;
        System.out.printf("Servidor Calculadora '%s' iniciado em %s:%d%n", serverName, host, port);
        if (DebugConfig.DEBUG_MODE) {
            System.out.println("🐛 DEBUG MODE: ENABLED");
        }
    }

    @Override
    public void registerWithDirectory(String directoryHost, int directoryPort) throws IOException {
        try (Socket socket = new Socket(directoryHost, directoryPort);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            String addressInfo = serverName + ":" + host + ":" + port;
            Message registerMsg = new Message(MessageType.REGISTER, "CalculatorService", addressInfo);
            
            output.println(registerMsg.pack());
            System.out.printf("Enviando REGISTER para diretório: %s:%d%n", directoryHost, directoryPort);
            
            String packedResponse = input.readLine();
            Message response = Message.unpack(packedResponse);
            
            if (response != null && response.getType() == MessageType.RESPONSE) {
                System.out.printf("Servidor '%s' registrado com sucesso no diretório!%n", serverName);
            } else {
                System.err.println("Falha ao registrar no diretório");
            }
        }
    }

    @Override
    public void start() {
        running = true;
        acceptClients();
    }

    private void acceptClients() {
        System.out.printf("Servidor '%s' aceitando conexões de clientes...%n", serverName);
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.printf("[%s] Cliente conectado: %s:%d%n", 
                    serverName, clientSocket.getInetAddress(), clientSocket.getPort());
                
                CalculatorHandler handler = new CalculatorHandler(clientSocket, serverName);
                handler.start();
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void shutdown() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.printf("Servidor '%s' desligado.%n", serverName);
            } catch (IOException e) {
                System.err.println("Erro ao desligar o servidor: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Uso: java CalculatorImpl <serverName> <host> <port> <directoryPort>");
            System.out.println("Exemplo: java CalculatorImpl Server-A localhost 6001 5000");
            System.exit(1);
        }

        String serverName = args[0];
        String host = args[1];
        int port = Integer.parseInt(args[2]);
        int directoryPort = Integer.parseInt(args[3]);

        try {
            CalculatorImpl calculator = new CalculatorImpl(serverName, host, port);
            calculator.registerWithDirectory("localhost", directoryPort);
            calculator.start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor calculadora: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
