package com.project.crypto;

public class MessageExample {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║      Mini-DNS Message Protocol Test Suite                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
        
        testRequestResponse();
        testRegisterUpdate();
        testSecurityRejection();
        testNullHandling();
        
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                  All Tests Complete!                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
    
    public static void testRequestResponse() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: REQUEST/RESPONSE Flow (Query Client Scenario)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        // Client sends REQUEST
        System.out.println("📤 CLIENT → SERVER: Request IP for 'servidor1'");
        Message request = new Message(MessageType.REQUEST, "servidor1", null);
        System.out.println("   Plain message: " + request.serialize());
        
        String packedRequest = request.pack();
        System.out.println("   Packed (encrypted||hmac):");
        System.out.println("   " + packedRequest.substring(0, Math.min(80, packedRequest.length())) + "...");
        System.out.println("   Length: " + packedRequest.length() + " chars\n");
        
        // Server receives and unpacks
        System.out.println("📥 SERVER: Receiving and unpacking message...");
        Message receivedRequest = Message.unpack(packedRequest);
        
        if (receivedRequest != null) {
            System.out.println("   ✅ HMAC verified successfully!");
            System.out.println("   ✅ Message decrypted successfully!");
            System.out.println("   Type: " + receivedRequest.getType());
            System.out.println("   Server Name: " + receivedRequest.getServerName());
            System.out.println("   IP Address: " + receivedRequest.getIpAddress());
        } else {
            System.out.println("   ❌ Failed to unpack message!");
        }
        
        // Server sends RESPONSE
        System.out.println("\n📤 SERVER → CLIENT: Response with IP address");
        Message response = new Message(MessageType.RESPONSE, "servidor1", "192.168.0.10");
        System.out.println("   Plain message: " + response.serialize());
        
        String packedResponse = response.pack();
        System.out.println("   Packed (encrypted||hmac):");
        System.out.println("   " + packedResponse.substring(0, Math.min(80, packedResponse.length())) + "...\n");
        
        // Client receives response
        System.out.println("📥 CLIENT: Receiving response...");
        Message receivedResponse = Message.unpack(packedResponse);
        
        if (receivedResponse != null) {
            System.out.println("   ✅ Success! Received IP: " + receivedResponse.getIpAddress());
            System.out.println("   " + receivedResponse.getServerName() + " → " + receivedResponse.getIpAddress());
        } else {
            System.out.println("   ❌ Failed to unpack response!");
        }
        
        System.out.println();
    }
    
    public static void testRegisterUpdate() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: REGISTER/UPDATE Flow (Registrar Scenario)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        // Registrar sends REGISTER
        System.out.println("📤 REGISTRAR → SERVER: Update servidor4 to new IP");
        Message register = new Message(MessageType.REGISTER, "servidor4", "192.168.0.99");
        System.out.println("   Plain message: " + register.serialize());
        
        String packedRegister = register.pack();
        System.out.println("   Packed length: " + packedRegister.length() + " chars\n");
        
        // Server receives
        System.out.println("📥 SERVER: Processing registration update...");
        Message receivedRegister = Message.unpack(packedRegister);
        
        if (receivedRegister != null) {
            System.out.println("   ✅ Registration accepted!");
            System.out.println("   Updating DNS: " + receivedRegister.getServerName() + 
                             " → " + receivedRegister.getIpAddress());
        }
        
        // Server broadcasts UPDATE to all clients
        System.out.println("\n📡 SERVER → ALL CLIENTS: Broadcasting update...");
        Message update = new Message(MessageType.UPDATE, "servidor4", "192.168.0.99");
        String packedUpdate = update.pack();
        
        System.out.println("📥 CLIENT 1: Received update");
        Message client1Update = Message.unpack(packedUpdate);
        if (client1Update != null) {
            System.out.println("   ✅ " + client1Update.getServerName() + " changed to " + 
                             client1Update.getIpAddress());
        }
        
        System.out.println("📥 CLIENT 2: Received update");
        Message client2Update = Message.unpack(packedUpdate);
        if (client2Update != null) {
            System.out.println("   ✅ " + client2Update.getServerName() + " changed to " + 
                             client2Update.getIpAddress());
        }
        
        System.out.println("📥 CLIENT 3: Received update");
        Message client3Update = Message.unpack(packedUpdate);
        if (client3Update != null) {
            System.out.println("   ✅ " + client3Update.getServerName() + " changed to " + 
                             client3Update.getIpAddress());
        }
        
        System.out.println();
    }
    
    public static void testSecurityRejection() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: Security - Wrong Key/Tampered Message Rejection");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        // Create valid message
        Message validMessage = new Message(MessageType.REQUEST, "servidor1", null);
        String packedMessage = validMessage.pack();
        
        System.out.println("📤 Attacker attempts to tamper with message...");
        
        // Tamper with HMAC (simulate wrong key or modification)
        String[] parts = packedMessage.split("\\|\\|");
        String tamperedHmac = "0000000000000000000000000000000000000000000000000000000000000000";
        String tamperedMessage = parts[0] + "||" + tamperedHmac;
        
        System.out.println("📥 SERVER: Receiving tampered message...");
        Message result = Message.unpack(tamperedMessage);
        
        if (result == null) {
            System.out.println("   ✅ SUCCESS! Server correctly rejected tampered message");
            System.out.println("   Message was discarded without decryption");
        } else {
            System.out.println("   ❌ SECURITY FAILURE! Tampered message was accepted!");
        }
        
        System.out.println("\n📤 Attacker attempts to modify encrypted data...");
        
        // Tamper with encrypted part
        String tamperedEncrypted = "InvalidBase64Data==" + "||" + parts[1];
        
        System.out.println("📥 SERVER: Receiving message with modified ciphertext...");
        Message result2 = Message.unpack(tamperedEncrypted);
        
        if (result2 == null) {
            System.out.println("   ✅ SUCCESS! Server correctly rejected modified ciphertext");
            System.out.println("   HMAC verification prevented processing of bad data");
        } else {
            System.out.println("   ❌ SECURITY FAILURE! Modified ciphertext was accepted!");
        }
        
        System.out.println();
    }
    
    public static void testNullHandling() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 4: Edge Cases - Null Values & Format Validation");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        System.out.println("📋 Testing REQUEST with null IP (expected for queries):");
        Message request = new Message(MessageType.REQUEST, "servidor5", null);
        String packed = request.pack();
        Message unpacked = Message.unpack(packed);
        
        if (unpacked != null) {
            System.out.println("   ✅ Serialization: " + unpacked.serialize());
            System.out.println("   Server: " + unpacked.getServerName());
            System.out.println("   IP: " + unpacked.getIpAddress());
        }
        
        System.out.println("\n📋 Testing invalid format (missing parts):");
        Message invalid = Message.unpack("InvalidFormat");
        if (invalid == null) {
            System.out.println("   ✅ Correctly rejected malformed message");
        }
        
        System.out.println("\n📋 Testing all message types:");
        MessageType[] types = {MessageType.REQUEST, MessageType.RESPONSE, 
                               MessageType.REGISTER, MessageType.UPDATE};
        
        for (MessageType type : types) {
            Message msg = new Message(type, "servidor1", "192.168.0.10");
            String p = msg.pack();
            Message u = Message.unpack(p);
            
            if (u != null && u.getType() == type) {
                System.out.println("   ✅ " + type + " - pack/unpack successful");
            } else {
                System.out.println("   ❌ " + type + " - FAILED");
            }
        }
        
        System.out.println();
    }
}
