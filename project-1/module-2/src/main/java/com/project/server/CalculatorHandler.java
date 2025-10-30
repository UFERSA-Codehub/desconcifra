package com.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.project.crypto.Message;
import com.project.crypto.MessageType;

public class CalculatorHandler extends Thread {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String serverName;

    public CalculatorHandler(Socket socket, String serverName) {
        this.socket = socket;
        this.serverName = serverName;
        try {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Erro ao configurar streams de E/S: " + e.getMessage());
        }
    }

    private void handleCalculate(Message message) {
        String operation = message.getOperation();
        double[] operands = message.getOperands();

        if (operands == null || operands.length != 2) {
            Message errorMsg = new Message(MessageType.ERROR, operation, "INVALID_OPERANDS");
            output.println(errorMsg.pack());
            return;
        }

        double op1 = operands[0];
        double op2 = operands[1];
        double result = 0.0;
        boolean success = true;

        switch (operation.toUpperCase()) {
            case "ADD":
                result = op1 + op2;
                System.out.printf("[%s] Calculando: %.2f + %.2f = %.2f%n", serverName, op1, op2, result);
                break;
            case "SUB":
            case "SUBTRACT":
                result = op1 - op2;
                System.out.printf("[%s] Calculando: %.2f - %.2f = %.2f%n", serverName, op1, op2, result);
                break;
            case "MUL":
            case "MULTIPLY":
                result = op1 * op2;
                System.out.printf("[%s] Calculando: %.2f * %.2f = %.2f%n", serverName, op1, op2, result);
                break;
            case "DIV":
            case "DIVIDE":
                if (op2 == 0) {
                    Message errorMsg = new Message(MessageType.ERROR, operation, "DIVISION_BY_ZERO");
                    output.println(errorMsg.pack());
                    System.out.printf("[%s] Erro: Divisão por zero%n", serverName);
                    return;
                }
                result = op1 / op2;
                System.out.printf("[%s] Calculando: %.2f / %.2f = %.2f%n", serverName, op1, op2, result);
                break;
            default:
                Message errorMsg = new Message(MessageType.ERROR, operation, "UNKNOWN_OPERATION");
                output.println(errorMsg.pack());
                System.out.printf("[%s] Operação desconhecida: %s%n", serverName, operation);
                success = false;
        }

        if (success) {
            Message response = Message.createResponseMessage(operation, result);
            output.println(response.pack());
        }
    }

    @Override
    public void run() {
        try {
            String packedMessage;
            while ((packedMessage = input.readLine()) != null) {
                Message message = Message.unpack(packedMessage);

                if (message == null) {
                    System.out.printf("[%s] ERRO: Mensagem inválida (Verificação HMAC falhou)%n", serverName);
                    continue;
                }

                System.out.printf("[%s] Mensagem recebida: %s%n", serverName, message.serialize());

                if (message.getType() == MessageType.CALCULATE) {
                    handleCalculate(message);
                }
            }
        } catch (IOException e) {
            System.err.printf("[%s] Erro na comunicação com o cliente: %s%n", serverName, e.getMessage());
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
