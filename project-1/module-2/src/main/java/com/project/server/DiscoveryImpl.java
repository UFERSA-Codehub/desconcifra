package com.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.project.crypto.DebugConfig;

public class DiscoveryImpl implements Discovery {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private Map<String, List<ServiceInfo>> servicesTable;
    private List<DiscoveryClientHandler> clientHandlers;
    private Map<String, AtomicInteger> roundRobinCounters;
    private Random random;
    private boolean running;

    public DiscoveryImpl(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.servicesTable = new ConcurrentHashMap<>();
        this.clientHandlers = Collections.synchronizedList(new ArrayList<>());
        this.roundRobinCounters = new ConcurrentHashMap<>();
        this.random = new Random();
        this.running = false;
        System.out.println("Servidor de descoberta iniciado na porta " + port);
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
                DiscoveryClientHandler clientHandler = new DiscoveryClientHandler(clientSocket, this);
                clientHandler.start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao aceitar conexão de cliente: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public ServiceInfo selectServer(String serviceName, String strategy) {
        List<ServiceInfo> servers = servicesTable.get(serviceName);
        
        if (servers == null || servers.isEmpty()) {
            return null;
        }

        if (servers.size() == 1) {
            return servers.get(0);
        }

        if ("random".equalsIgnoreCase(strategy)) {
            int index = random.nextInt(servers.size());
            ServiceInfo selected = servers.get(index);
            System.out.printf("Load balancing (RANDOM): Selected %s for service '%s'%n", 
                selected.getServerName(), serviceName);
            return selected;
        } else {
            roundRobinCounters.putIfAbsent(serviceName, new AtomicInteger(0));
            AtomicInteger counter = roundRobinCounters.get(serviceName);
            int index = counter.getAndIncrement() % servers.size();
            ServiceInfo selected = servers.get(index);
            System.out.printf("Load balancing (ROUND-ROBIN): Selected %s for service '%s' (index=%d)%n", 
                selected.getServerName(), serviceName, index);
            return selected;
        }
    }

    @Override
    public void start() {
        running = true;
        acceptClients();
    }

    @Override
    public synchronized void discoverServices() {
        System.out.println("Descobrindo serviços registrados...");
        for (Map.Entry<String, List<ServiceInfo>> entry : servicesTable.entrySet()) {
            System.out.printf("Serviço: %s (%d servidores)%n", entry.getKey(), entry.getValue().size());
            for (ServiceInfo info : entry.getValue()) {
                System.out.printf("  - %s%n", info);
            }
        }
    }

    @Override
    public synchronized void registerService(String serviceName, String serverName, String host, int port) {
        ServiceInfo serviceInfo = new ServiceInfo(serviceName, serverName, host, port);
        
        servicesTable.putIfAbsent(serviceName, Collections.synchronizedList(new ArrayList<>()));
        List<ServiceInfo> servers = servicesTable.get(serviceName);
        servers.add(serviceInfo);

        System.out.printf("Serviço registrado: %s%n", serviceInfo);
    }

    @Override
    public synchronized void unregisterService(String serviceName, String serverName) {
        List<ServiceInfo> servers = servicesTable.get(serviceName);
        
        if (servers != null) {
            servers.removeIf(info -> info.getServerName().equals(serverName));
            
            if (servers.isEmpty()) {
                servicesTable.remove(serviceName);
            }
            
            System.out.printf("Serviço removido: %s/%s%n", serviceName, serverName);
        }
    }

    @Override
    public Map<String, List<ServiceInfo>> getServicesTable() {
        return servicesTable;
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
            DiscoveryImpl server = new DiscoveryImpl(PORT);
            server.start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
