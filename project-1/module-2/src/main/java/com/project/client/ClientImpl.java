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
    private boolean running;

    private Thread listenerThread;
    private Message pendingCalculateMessage;

    public ClientImpl(String[] args) {
        this.isRegister = parseArgs(args);
        this.running = false;
    }

    private boolean parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--register")) {
                return true;
            }
        }
        return false;
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

                    if (message.getType() == MessageType.REDIRECT) {
                        handleRedirect(message);

                    } else if (message.getType() == MessageType.RESPONSE) {
                        if (message.getIpAddress() != null && message.getIpAddress().equals("REGISTERED")) {
                            System.out.printf("Serviço %s registrado com sucesso!%n", message.getServerName());
                        } else {
                            try {
                                double result = message.getResult();
                                System.out.printf("Resultado: %s = %.2f%n", message.getOperation(), result);
                            } catch (NumberFormatException e) {
                                System.out.printf("Resposta recebida: %s -> %s%n", message.getServerName(),
                                        message.getIpAddress());
                            }
                        }
                        System.out.print("> ");

                    } else if (message.getType() == MessageType.ERROR) {
                        System.out.printf("ERRO: %s - %s%n", message.getServerName(), message.getIpAddress());
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
        System.out.println("║   Service Discovery Client     ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.println("╔════════════════════════════════╗");
        System.out.println("║ Mode: " + (isRegister ? "REGISTER (Full Access)   ║" : "CLIENT (Service User)    ║"));
        System.out.println("╚════════════════════════════════╝");
        if (DebugConfig.DEBUG_MODE) {
            System.out.println("🐛 DEBUG MODE: ENABLED");
        }
        System.out.println("\n📋 Available Commands:");
        System.out.println("  CALCULATE <op> <n1> <n2>       - Calculate (ADD/SUB/MUL/DIV)");
        if (isRegister) {
            System.out.println("  REGISTER <serviceName> <info>  - Register service");
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
            case "CALCULATE":
                if (parts.length != 4) {
                    System.out.println("Usage: CALCULATE <operation> <operand1> <operand2>");
                    System.out.println("Operations: ADD, SUB, MUL, DIV");
                    break;
                }
                sendCalculate(parts[1], parts[2], parts[3]);
                break;

            case "REGISTER":
                if (parts.length < 3) {
                    System.out.println("Usage: REGISTER <serviceName> <info>");
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
            if (DebugConfig.DEBUG_MODE) {
                System.out.printf("Conectado ao servidor em %s:%d%n", host, port);
            }
        } catch (IOException e) {
            System.err.printf("Erro ao conectar ao servidor: %s%n", e.getMessage());
        }

    }

    @Override
    public void sendRequest(String serverName) {
        Message message = new Message(MessageType.REGISTER, serverName, "");
        output.println(message.pack());
        if (DebugConfig.DEBUG_MODE) {
            System.out.println("");
            System.out.println("Mensagem enviada: " + message.serialize());
        }
    }

    public void sendCalculate(String operation, String op1Str, String op2Str) {
        try {
            double op1 = Double.parseDouble(op1Str);
            double op2 = Double.parseDouble(op2Str);

            Message message = Message.createCalculateMessage(operation.toUpperCase(), op1, op2);
            pendingCalculateMessage = message;
            output.println(message.pack());
            if (DebugConfig.DEBUG_MODE) {
                System.out.println("");
                System.out.println("Mensagem enviada: " + message.serialize());
            }
        } catch (NumberFormatException e) {
            System.out.println("Erro: Operandos devem ser números válidos");
        }
    }

    private void handleRedirect(Message message) {
        String serverName = message.getServerName();
        String address = message.getIpAddress();

        if (address == null || !address.contains(":")) {
            System.out.println("ERRO: Endereço de redirecionamento inválido");
            return;
        }

        String[] parts = address.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        if (DebugConfig.DEBUG_MODE) {
            System.out.printf("🔄 Redirecionando para %s em %s:%d...%n", serverName, host, port);
        }

        disconnect();

        connect(host, port);
        this.running = true;
        startListener();

        if (pendingCalculateMessage != null) {
            output.println(pendingCalculateMessage.pack());
            if (DebugConfig.DEBUG_MODE) {
                System.out.println("Reenviando cálculo para o servidor...");
            }
            pendingCalculateMessage = null;
        }
    }

    @Override
    public void sendRegister(String serverName, String ipAddress) {
        if (!isRegister) {
            System.out.println("Modo de registro não ativado. Use --register para ativar.");
            return;
        }
        Message message = new Message(MessageType.REGISTER, serverName, ipAddress);
        output.println(message.pack());
        if (DebugConfig.DEBUG_MODE) {
            System.out.println("");
            System.out.println("Mensagem enviada: " + message.serialize());
        }
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

            if (DebugConfig.DEBUG_MODE) {
                System.out.println("Desconectado do servidor.");
            }
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
