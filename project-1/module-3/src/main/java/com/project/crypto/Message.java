package com.project.crypto;

public class Message {
    private MessageType type;
    private int originNodeId;
    private int currentNodeId;
    private String fileName;
    private int hopCount;
    private int maxHops;

    public Message(MessageType type, int originNodeId, int currentNodeId, String fileName, int hopCount, int maxHops) {
        this.type = type;
        this.originNodeId = originNodeId;
        this.currentNodeId = currentNodeId;
        this.fileName = fileName;
        this.hopCount = hopCount;
        this.maxHops = maxHops;
    }

    public static Message createSearchMessage(int originNodeId, String fileName) {
        return new Message(MessageType.SEARCH, originNodeId, originNodeId, fileName, 0, 6);
    }

    public static Message createFoundMessage(int originNodeId, int ownerNodeId, String fileName) {
        return new Message(MessageType.FOUND, originNodeId, ownerNodeId, fileName, 0, 6);
    }

    public static Message createNotFoundMessage(int originNodeId, String fileName) {
        return new Message(MessageType.NOT_FOUND, originNodeId, -1, fileName, 6, 6);
    }

    public String serialize() {
        return String.format("%s|%d|%d|%s|%d|%d", 
            type.name(), originNodeId, currentNodeId, fileName, hopCount, maxHops);
    }

    public static Message deserialize(String data) {
        String[] parts = data.split("\\|");
        if (parts.length != 6) {
            return null;
        }
        
        try {
            MessageType type = MessageType.valueOf(parts[0]);
            int originNodeId = Integer.parseInt(parts[1]);
            int currentNodeId = Integer.parseInt(parts[2]);
            String fileName = parts[3];
            int hopCount = Integer.parseInt(parts[4]);
            int maxHops = Integer.parseInt(parts[5]);
            
            return new Message(type, originNodeId, currentNodeId, fileName, hopCount, maxHops);
        } catch (Exception e) {
            return null;
        }
    }

    public String pack() {
        String serialized = serialize();
        
        AES aes = new AES();
        aes.setChave(KeyManager.getAESKey());
        String encrypted = aes.cifrar(serialized);
        
        HMAC hmac = new HMAC();
        hmac.setKey(KeyManager.getHMACKey());
        String hmacValue = hmac.generateHMAC(encrypted);
        
        return encrypted + "|" + hmacValue;
    }

    public static Message unpack(String packed) {
        try {
            String[] parts = packed.split("\\|");
            if (parts.length != 2) {
                return null;
            }
            
            String encryptedMessage = parts[0];
            String receivedHmac = parts[1];
            
            HMAC hmac = new HMAC();
            hmac.setKey(KeyManager.getHMACKey());
            
            if (DebugConfig.DEBUG_MODE) {
                String computedHmac = hmac.generateHMAC(encryptedMessage);
                System.out.println("--- DEBUG INFO ---");
                System.out.println("DEBUG: Encrypted message: " + encryptedMessage.substring(0, Math.min(50, encryptedMessage.length())) + "...");
                System.out.println("DEBUG: Received HMAC:    " + receivedHmac);
                System.out.println("DEBUG: Computed HMAC:    " + computedHmac);
                System.out.println("DEBUG: HMAC match:       " + computedHmac.equals(receivedHmac));
                System.out.println("-------------------");
            }
            
            boolean isValid = hmac.verifyHMAC(encryptedMessage, receivedHmac);
            
            if (!isValid) {
                return null;
            }
            
            AES aes = new AES();
            aes.setChave(KeyManager.getAESKey());
            String decrypted = aes.decifrar(encryptedMessage);
            
            if (decrypted == null) {
                return null;
            }
            
            return deserialize(decrypted);
        } catch (Exception e) {
            return null;
        }
    }

    public Message incrementHop(int currentNodeId) {
        return new Message(this.type, this.originNodeId, currentNodeId, this.fileName, this.hopCount + 1, this.maxHops);
    }

    public MessageType getType() {
        return type;
    }

    public int getOriginNodeId() {
        return originNodeId;
    }

    public int getCurrentNodeId() {
        return currentNodeId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getHopCount() {
        return hopCount;
    }

    public int getMaxHops() {
        return maxHops;
    }
}
