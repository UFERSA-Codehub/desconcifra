package com.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.project.crypto.DebugConfig;

public class ServerImpl implements Server {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private Map<String, String> dnsTable;
    private List<ClientHandler> clientHandlers;
    private boolean running;

    public ServerImpl(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.dnsTable = new ConcurrentHashMap<>();
        this.clientHandlers = Collections.synchronizedList(new ArrayList<>());
        this.running = false;
        initDNSTable();
        System.out.println("Servidor iniciado na porta " + port);
        if (DebugConfig.DEBUG_MODE) {
            System.out.println("🐛 DEBUG MODE: ENABLED");
        }
    }

    private void acceptClients() {
        // Lógica para aceitar conexões de clientes
        System.out.println("Aceitando conexões de clientes...");

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandler.start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao aceitar conexão de cliente: " + e.getMessage());
                }
            }
        }
    }

    private void initDNSTable() {
        for (int i = 1; i <= 10; i++) {
            String serverName = "servidor" + i;
            String ipAddress = "192.168.0." + (i*10);
            dnsTable.put(serverName, ipAddress);
            System.out.printf("Entrada adicionada: %s -> %s%n", serverName, ipAddress);
        }
    }

    @Override
    public void start() {
        running = true;
        acceptClients();
    }

    @Override
    public synchronized String lookup(String serverName) {
        return dnsTable.get(serverName);
    }

    @Override
    public synchronized void updateEntry(String serverName, String newIP) {
        String oldIP = dnsTable.put(serverName, newIP);

        System.out.printf("Entrada atualizada: %s:%s -> %s%n", serverName, oldIP, newIP);

        // Notificar clientes sobre a atualização
        broadcast(serverName, newIP);
    }

    @Override
    public synchronized void broadcast(String serverName, String newIP) {
        System.out.println("Broadcasting update to clients...");
        // sleep(1000); // Simula atraso na rede
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendUpdate(serverName, newIP);
        }
    }

    @Override
    public synchronized void addRequestClient(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
        System.out.printf("Cliente adicionado para receber atualizações: %s. Total de clientes: %d%n",
                clientHandler, clientHandlers.size());
    }

    @Override
    public synchronized void removeRequestClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.printf("Cliente removido das atualizações: %s. Total de clientes: %d%n",
                clientHandler, clientHandlers.size());
    }

    @Override
    public void shutdown() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Servidor desligado.");
            } catch (IOException e) {
                System.err.println("Erro ao desligar o servidor: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerImpl server = new ServerImpl(PORT);
            server.start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
