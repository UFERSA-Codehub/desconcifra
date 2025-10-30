package com.project.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyManager {
    

    private static final String SHARED_SECRET = "MinhaSeedMassa";

    private static SecretKey aesKey = null;
    private static SecretKey hmacKey = null;

    public static SecretKey getAESKey() {
        if(aesKey == null) {
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] hashedSecret = sha256.digest(SHARED_SECRET.getBytes(StandardCharsets.UTF_8));
                byte[] aesKeyBytes = Arrays.copyOfRange(hashedSecret, 0, 16); // AES-128
                aesKey = new SecretKeySpec(aesKeyBytes, "AES");
                if (DebugConfig.DEBUG_MODE) {
                    System.out.println("");
                    System.out.println("🔑 AES Key (" + aesKeyBytes.length + " bytes): " + bytesToHex(aesKeyBytes));
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 deu algo", e);
            }

        }
        return aesKey;
    }

    public static SecretKey getHMACKey() {
        if(hmacKey == null) {
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] hashedSecret = sha256.digest(SHARED_SECRET.getBytes(StandardCharsets.UTF_8));
                byte[] hmacKeyBytes = Arrays.copyOfRange(hashedSecret, 0, 32); // Usar os 32 bytes do secret para HMAC
                hmacKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA256");
                if (DebugConfig.DEBUG_MODE) {
                    System.out.println("🔑 HMAC Key (" + hmacKeyBytes.length + " bytes): " + bytesToHex(hmacKeyBytes));
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 deu algo", e);
            }
        }
        
        return hmacKey;
    }

    private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}
}
