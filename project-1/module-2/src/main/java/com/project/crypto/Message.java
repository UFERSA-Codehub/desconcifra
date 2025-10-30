package com.project.crypto;

public class Message {
    private MessageType type;
    private String serverName;
    private String ipAddress;

    public Message(MessageType type, String serverName, String ipAddress) {
        this.type = type;
        this.serverName = serverName;
        this.ipAddress = ipAddress;

    }

    public Message(MessageType type, String serverName) {
        this(type, serverName, null);
    }

    public static Message createCalculateMessage(String operation, double operand1, double operand2) {
        String operands = operand1 + "," + operand2;
        return new Message(MessageType.CALCULATE, operation, operands);
    }

    public static Message createResponseMessage(String operation, double result) {
        return new Message(MessageType.RESPONSE, operation, String.valueOf(result));
    }

    public static Message createRedirectMessage(String serverName, String host, int port) {
        String address = host + ":" + port;
        return new Message(MessageType.REDIRECT, serverName, address);
    }

    public MessageType getType() {
        return type;
    }

    public String getServerName() {
        return serverName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getOperation() {
        return serverName;
    }

    public double[] getOperands() {
        if (ipAddress == null) {
            return null;
        }
        String[] parts = ipAddress.split(",");
        if (parts.length != 2) {
            return null;
        }
        return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
    }

    public double getResult() {
        if (ipAddress == null) {
            return 0.0;
        }
        return Double.parseDouble(ipAddress);
    }

    public String serialize() {
        return type.name() + "||" + serverName + "||" + ipAddress;
    }

    public static Message deserialize(String data) {
        String[] parts = data.split("\\|\\|");

        MessageType type = MessageType.valueOf(parts[0]);
        String serverName = parts.length > 1 ? parts[1] : null;
        String ipAddress = parts.length > 2 ? parts[2] : null;

        return new Message(type, serverName, ipAddress);
    }

    public String pack() {
        String normalMessage = this.serialize();

        AES aes = new AES();
        aes.setChave(KeyManager.getAESKey());
        String encryptedMessage = aes.cifrar(normalMessage);

        HMAC hmac = new HMAC();
        hmac.setKey(KeyManager.getHMACKey());
        String hmacValue = hmac.generateHMAC(encryptedMessage);

        return encryptedMessage + "||" + hmacValue;
    }

    public static Message unpack(String packedMessage) {
        String[] parts = packedMessage.split("\\|\\|");
        if (parts.length != 2) {
            return null;
        }

        String encryptedMessage = parts[0];
        String receivedHmac = parts[1];

        HMAC hmac = new HMAC();
        hmac.setKey(KeyManager.getHMACKey());
        String computedHmac = hmac.generateHMAC(encryptedMessage);
        
        if (DebugConfig.DEBUG_MODE) {
            System.out.println("DEBUG: Encrypted message: " + encryptedMessage);
            System.out.println("DEBUG: Received HMAC:    " + receivedHmac);
            System.out.println("DEBUG: Computed HMAC:    " + computedHmac);
            System.out.println("DEBUG: HMAC match:       " + computedHmac.equals(receivedHmac));
        }
        
        boolean isValid = hmac.verifyHMAC(encryptedMessage, receivedHmac);

        if (!isValid) {
            System.out.println("HMAC verification failed. FAKE NEWSSON?");
            return null;

        }

        AES aes = new AES();
        aes.setChave(KeyManager.getAESKey());
        String normalMessage = aes.decifrar(encryptedMessage);

        if (normalMessage == null) {
            System.out.println("Decryption failed. FAKE NEWSSON? Probably silently caught exception.");
            return null;
        }

        return Message.deserialize(normalMessage);
    }

}
