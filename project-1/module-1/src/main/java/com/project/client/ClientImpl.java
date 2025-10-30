package com.project.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.project.crypto.DebugConfig;
import com.project.crypto.Message;
import com.project.crypto.MessageType;

public class ClientImpl implements Client {

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private boolean isRegister;
    private boolean isMalicious;
    private boolean running;

    private Thread listenerThread;

    public ClientImpl(String[] args) {
       parseArgs(args);
        this.running = false;

        if(this.isMalicious){
            com.project.crypto.KeyManager.corruptHMACKey();
            System.out.println("⚠️ Modo Malicioso Ativado: Chave HMAC será corrompida.");
        }
    }

    private void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--register")) {
                this.isRegister = true;
            }
            if (arg.equalsIgnoreCase("--malicious")) {
                this.isMalicious = true;
            }
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                while (running) {
                    String packedMessage = input.readLine();

                    if (packedMessage == null) {
                        System.out.println("Conexão encerrada pelo servidor.");
                        running = false;
                        break;
                    }

                    Message message = Message.unpack(packedMessage);

                    if (message == null) {
                        System.out.println("ERRO: Mensagem inválida (Verificação HMAC falhou)");
                        continue;
                    }

                    if (message.getType() == MessageType.RESPONSE) {
                        if (message.getIpAddress().equals("NOT_FOUND")) {
                            System.out.printf("Servidor %s não encontrado.%n", message.getServerName());
                        } else {
                            System.out.printf("Resposta recebida: %s -> %s%n", message.getServerName(),
                                    message.getIpAddress());
                        }
                        System.out.print("> ");
                        
                    } else if (message.getType() == MessageType.UPDATE) {
                        System.out.printf("Atualização recebida: %s -> %s%n", message.getServerName(),
                                message.getIpAddress());
                        System.out.print("> ");
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.printf("Erro ao ler mensagem do servidor: %s%n", e.getMessage());
                }
            }
        });

        listenerThread.start();
    }

    private void Menu() {
        clearScreen();
        Scanner scanner = new Scanner(System.in);

        welcomeMessage();

        while (running) {
            
            System.out.println("");
            System.out.print("> ");
            String inputLine = scanner.nextLine().trim();

            if (inputLine.isEmpty()) {
                continue;
            }

            processCommand(inputLine);
        }

        scanner.close();
    }

    private void welcomeMessage() {
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║      Secure DNS Client         ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║ Mode: " + (isRegister ? "REGISTER (Full Access)   ║" : "REQUEST (Read-Only)      ║"));
        System.out.println("╚════════════════════════════════╝");
        if (isMalicious){
            System.out.println("⚠️  MALICIOUS MODE ENABLED ⚠️" );
        }
        if (DebugConfig.DEBUG_MODE) {
            System.out.println("🐛 DEBUG MODE: ENABLED");
        }
        System.out.println("\n📋 Available Commands:");
        System.out.println("  REQUEST <serverName>           - Lookup DNS entry");
        if (isRegister) {
            System.out.println("  REGISTER <serverName> <ip>     - Update DNS entry");
        }
        System.out.println("  HELP                           - Show this help");
        System.out.println("  EXIT                           - Disconnect and quit");
        System.out.println();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void processCommand(String inputLine) {
        String[] parts = inputLine.split("\\s+");
        String command = parts[0].toUpperCase();

        switch (command) {
            case "REQUEST":
                if (parts.length != 2) {
                    System.out.println("Usage: REQUEST <serverName>");
                    break;
                }
                sendRequest(parts[1]);
                break;
                
            case "REGISTER":
                if (parts.length != 3) {
                    System.out.println("Usage: REGISTER <serverName> <ip>");
                    break;
                }
                sendRegister(parts[1], parts[2]);
                break;

            case "HELP":
                welcomeMessage();
                break;
            case "EXIT":
                System.out.print("Disconnecting");
                try {
                    for (int i = 0; i < 3; i++) {
                        Thread.sleep(400);
                        System.out.print(".");
                        System.out.flush();
                    }
                    System.out.println();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Disconnect interrupted: " + e.getMessage());
                }
                disconnect();
                break;
            default:
                System.out.println("Unknown command. " + command + ". \nType HELP for a list of commands.");
        }
    }

    @Override
    public void connect(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.output = new PrintWriter(socket.getOutputStream(), true);
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.printf("Conectado ao servidor em %s:%d%n", host, port);
        } catch (IOException e) {
            System.err.printf("Erro ao conectar ao servidor: %s%n", e.getMessage());
        }

    }

    @Override
    public void sendRequest(String serverName) {
        Message message = new Message(MessageType.REQUEST, serverName, "");
        output.println(message.pack());
        System.out.println("");
        System.out.println("Mensagem enviada: " + message.serialize());
    }

    @Override
    public void sendRegister(String serverName, String ipAddress) {
        if (!isRegister) {
            System.out.println("Modo de registro não ativado. Use --register para ativar.");
            return;
        }
        Message message = new Message(MessageType.REGISTER, serverName, ipAddress);
        output.println(message.pack());
        System.out.println("");
        System.out.println("Mensagem enviada: " + message.serialize());
    }

    @Override
    public void start() {
        this.running = true;
        startListener();
        Menu();
    }

    @Override
    public void disconnect() {
        this.running = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.join(1000);
            }

            System.out.println("Desconectado do servidor.");
        } catch (IOException | InterruptedException e) {
            System.err.printf("Erro ao desconectar: %s%n", e.getMessage());
        }
    }



    public static void main(String[] args) {
        ClientImpl client = new ClientImpl(args);
        client.connect("localhost", 5000);
        client.start();
    }

}
