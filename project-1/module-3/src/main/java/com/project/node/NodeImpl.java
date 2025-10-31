package com.project.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.project.crypto.DebugConfig;
import com.project.crypto.KeyManager;
import com.project.crypto.Message;
import com.project.crypto.MessageType;
import com.project.file.FileStorage;

public class NodeImpl implements Node {
    private static final int BASE_PORT = 5000;
    
    private int nodeId;
    private int port;
    private int successorPort;
    private int predecessorPort;
    private boolean isMalicious;
    
    private ServerSocket serverSocket;
    private FileStorage fileStorage;
    private boolean running;
    
    private ConcurrentHashMap<String, Long> messageLog;
    private AtomicInteger messagesReceived;
    private AtomicInteger messagesSent;

    public NodeImpl(int nodeId, boolean isMalicious) {
        this.nodeId = nodeId;
        this.port = BASE_PORT + nodeId;
        this.successorPort = BASE_PORT + ((nodeId + 1) % 6);
        this.predecessorPort = BASE_PORT + ((nodeId - 1 + 6) % 6);
        this.isMalicious = isMalicious;
        this.fileStorage = new FileStorage(nodeId);
        this.running = false;
        this.messageLog = new ConcurrentHashMap<>();
        this.messagesReceived = new AtomicInteger(0);
        this.messagesSent = new AtomicInteger(0);
        
        if (isMalicious) {
            KeyManager.corruptHMACKey();
        }
    }

    @Override
    public void start() {
        running = true;
        startServer();
        showWelcomeMessage();
        startCLI();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.printf("🌐 Node P%d listening on port %d%n", nodeId, port);
                
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(() -> handleIncomingMessage(clientSocket)).start();
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error starting server: " + e.getMessage());
            }
        }).start();
    }

    private void handleIncomingMessage(Socket socket) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String packedMessage = input.readLine();
            
            if (packedMessage == null) {
                return;
            }
            
            Message message = Message.unpack(packedMessage);
            
            if (message == null) {
                System.out.println("❌ SECURITY: Message discarded (HMAC verification failed)");
                logMessage("RECEIVED", "INVALID", "HMAC_FAILED");
                return;
            }
            
            messagesReceived.incrementAndGet();
            
            if (DebugConfig.DEBUG_MODE) {
                System.out.printf("📨 RECEIVED: %s%n", message.serialize());
            }
            
            processMessage(message);
            
            socket.close();
        } catch (IOException e) {
            System.err.println("Error handling incoming message: " + e.getMessage());
        }
    }

    private void processMessage(Message message) {
        MessageType type = message.getType();
        
        if (type == MessageType.SEARCH) {
            handleSearchMessage(message);
        } else if (type == MessageType.FOUND) {
            handleFoundMessage(message);
        } else if (type == MessageType.NOT_FOUND) {
            handleNotFoundMessage(message);
        }
    }

    private void handleSearchMessage(Message message) {
        String fileName = message.getFileName();
        int originNodeId = message.getOriginNodeId();
        
        logMessage("RECEIVED", "SEARCH", fileName + " from P" + originNodeId);
        System.out.printf("🔍 SEARCH request for '%s' from P%d (hop %d)%n", 
            fileName, originNodeId, message.getHopCount());
        
        if (fileStorage.hasFile(fileName)) {
            System.out.printf("✅ FOUND: '%s' is stored on this node (P%d)%n", fileName, nodeId);
            logMessage("ACTION", "FOUND_LOCAL", fileName);
            
            Message foundMessage = Message.createFoundMessage(originNodeId, nodeId, fileName);
            sendToPredecessor(foundMessage);
            
        } else {
            if (message.getHopCount() >= message.getMaxHops() - 1) {
                System.out.printf("❌ NOT_FOUND: '%s' after full ring traversal%n", fileName);
                logMessage("ACTION", "NOT_FOUND_FULL_RING", fileName);
                
                if (originNodeId == nodeId) {
                    System.out.printf("🔴 File '%s' does not exist in the network%n", fileName);
                }
                return;
            }
            
            System.out.printf("➡️  FORWARDING search for '%s' to successor P%d%n", 
                fileName, (nodeId + 1) % 6);
            logMessage("ACTION", "FORWARD", fileName + " to P" + ((nodeId + 1) % 6));
            
            Message forwardMessage = message.incrementHop(nodeId);
            sendToSuccessor(forwardMessage);
        }
    }

    private void handleFoundMessage(Message message) {
        String fileName = message.getFileName();
        int ownerNodeId = message.getCurrentNodeId();
        int originNodeId = message.getOriginNodeId();
        
        logMessage("RECEIVED", "FOUND", fileName + " at P" + ownerNodeId);
        
        if (originNodeId == nodeId) {
            System.out.printf("🎉 SUCCESS: File '%s' found at P%d%n", fileName, ownerNodeId);
            System.out.print("> ");
        } else {
            System.out.printf("⬅️  RELAYING FOUND message for '%s' (found at P%d, requesting P%d)%n", 
                fileName, ownerNodeId, originNodeId);
            logMessage("ACTION", "RELAY_FOUND", fileName);
            sendToPredecessor(message);
        }
    }

    private void handleNotFoundMessage(Message message) {
        String fileName = message.getFileName();
        int originNodeId = message.getOriginNodeId();
        
        logMessage("RECEIVED", "NOT_FOUND", fileName);
        
        if (originNodeId == nodeId) {
            System.out.printf("🔴 File '%s' NOT FOUND in the network%n", fileName);
            System.out.print("> ");
        } else {
            System.out.printf("⬅️  RELAYING NOT_FOUND message for '%s'%n", fileName);
            logMessage("ACTION", "RELAY_NOT_FOUND", fileName);
            sendToPredecessor(message);
        }
    }

    private void sendToSuccessor(Message message) {
        sendMessage(message, successorPort, (nodeId + 1) % 6);
    }

    private void sendToPredecessor(Message message) {
        sendMessage(message, predecessorPort, (nodeId - 1 + 6) % 6);
    }

    private void sendMessage(Message message, int targetPort, int targetNodeId) {
        try {
            Socket socket = new Socket("localhost", targetPort);
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            
            String packed = message.pack();
            output.println(packed);
            
            messagesSent.incrementAndGet();
            
            if (DebugConfig.DEBUG_MODE) {
                System.out.printf("📤 SENT to P%d: %s%n", targetNodeId, message.serialize());
            }
            
            logMessage("SENT", message.getType().name(), 
                message.getFileName() + " to P" + targetNodeId);
            
            socket.close();
        } catch (IOException e) {
            System.err.printf("Error sending message to P%d (port %d): %s%n", 
                targetNodeId, targetPort, e.getMessage());
        }
    }

    @Override
    public void searchFile(String fileName) {
        if (!fileName.startsWith("arquivo")) {
            System.out.println("Erro na entrada de dados. Tente outra vez!");
            return;
        }
        
        System.out.printf("🔎 Initiating search for '%s' from P%d%n", fileName, nodeId);
        logMessage("INITIATED", "SEARCH", fileName);
        
        Message searchMessage = Message.createSearchMessage(nodeId, fileName);
        sendToSuccessor(searchMessage);
    }

    private void logMessage(String direction, String type, String details) {
        String timestamp = String.format("%tT", System.currentTimeMillis());
        String logEntry = String.format("[%s] %s - %s: %s", timestamp, direction, type, details);
        messageLog.put(logEntry, System.currentTimeMillis());
    }

    private void showWelcomeMessage() {
        clearScreen();
        System.out.println("╔════════════════════════════════════════╗");
        System.out.printf("║   P2P Ring Node - P%-2d (Port %-5d)     ║%n", nodeId, port);
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("╔════════════════════════════════════════╗");
        System.out.printf("║ Successor:   P%-2d (Port %-5d)          ║%n", (nodeId + 1) % 6, successorPort);
        System.out.printf("║ Predecessor: P%-2d (Port %-5d)          ║%n", (nodeId - 1 + 6) % 6, predecessorPort);
        System.out.println("╚════════════════════════════════════════╝");
        
        if (isMalicious) {
            System.out.println("⚠️  MALICIOUS MODE ENABLED ⚠️");
        }
        
        if (DebugConfig.DEBUG_MODE) {
            System.out.println(" DEBUG MODE: ENABLED");
        }
        
        System.out.printf("%n Files stored on this node (P%d):%n", nodeId);
        for (String file : fileStorage.getFiles()) {
            System.out.printf("   • %s%n", file);
        }
        
        System.out.println("\n Available Commands:");
        System.out.println("  SEARCH <fileName>  - Search for a file in the ring");
        System.out.println("  FILES              - Show files on this node");
        System.out.println("  LOG                - Show message log");
        System.out.println("  STATS              - Show statistics");
        System.out.println("  HELP               - Show this help");
        System.out.println("  EXIT               - Stop node and exit");
        System.out.println();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void startCLI() {
        Scanner scanner = new Scanner(System.in);
        
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            processCommand(input);
        }
        
        scanner.close();
    }

    private void processCommand(String input) {
        String[] parts = input.split("\\s+");
        String command = parts[0].toUpperCase();
        
        switch (command) {
            case "SEARCH":
                if (parts.length != 2) {
                    System.out.println("Usage: SEARCH <fileName>");
                    System.out.println("Example: SEARCH arquivo25");
                    break;
                }
                searchFile(parts[1]);
                break;
                
            case "FILES":
                showFiles();
                break;
                
            case "LOG":
                showLog();
                break;
                
            case "STATS":
                showStats();
                break;
                
            case "HELP":
                showWelcomeMessage();
                break;
                
            case "EXIT":
                stop();
                break;
                
            default:
                System.out.println("Erro na entrada de dados. Tente outra vez!");
        }
    }

    private void showFiles() {
        System.out.printf("%n📁 Files stored on P%d:%n", nodeId);
        for (String file : fileStorage.getFiles()) {
            System.out.printf("   • %s%n", file);
        }
        System.out.println();
    }

    private void showLog() {
        System.out.printf("%n📜 Message Log for P%d:%n", nodeId);
        if (messageLog.isEmpty()) {
            System.out.println("   (No messages yet)");
        } else {
            messageLog.keySet().stream()
                .sorted()
                .forEach(entry -> System.out.println("   " + entry));
        }
        System.out.println();
    }

    private void showStats() {
        System.out.printf("%n📊 Statistics for P%d:%n", nodeId);
        System.out.printf("   Messages Received: %d%n", messagesReceived.get());
        System.out.printf("   Messages Sent: %d%n", messagesSent.get());
        System.out.printf("   Total Messages: %d%n", messagesReceived.get() + messagesSent.get());
        System.out.println();
    }

    @Override
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.printf("👋 Node P%d stopped%n", nodeId);
        } catch (IOException e) {
            System.err.println("Error stopping node: " + e.getMessage());
        }
        System.exit(0);
    }

    @Override
    public int getNodeId() {
        return nodeId;
    }

    @Override
    public int getPort() {
        return port;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java NodeImpl <nodeId> [--malicious]");
            System.out.println("Example: java NodeImpl 0");
            System.out.println("Example: java NodeImpl 3 --malicious");
            System.exit(1);
        }
        
        try {
            int nodeId = Integer.parseInt(args[0]);
            
            if (nodeId < 0 || nodeId > 5) {
                System.out.println("Error: nodeId must be between 0 and 5");
                System.exit(1);
            }
            
            boolean isMalicious = false;
            for (String arg : args) {
                if (arg.equalsIgnoreCase("--malicious")) {
                    isMalicious = true;
                    break;
                }
            }
            
            NodeImpl node = new NodeImpl(nodeId, isMalicious);
            node.start();
            
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid nodeId. Must be a number between 0 and 5");
            System.exit(1);
        }
    }
}
