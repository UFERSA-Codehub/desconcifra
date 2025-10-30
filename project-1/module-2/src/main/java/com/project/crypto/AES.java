package com.project.crypto;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


 public class AES {
        private KeyGenerator geradorDeChaves;
        private SecretKey chave;

        public AES() {
            //gerarChave();
        }

        public void gerarChave() {
            try {
                geradorDeChaves = KeyGenerator
                        .getInstance("AES");
                chave = geradorDeChaves
                        .generateKey();
                System.out.println("Chave gerada: "
                        + chave.toString());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        public String cifrar(String textoAberto) {
            String mensagemCifrada = null;
            byte[] bytesMensagemCifrada;
            Cipher cifrador;
            // Encripta mensagem
            try {
                cifrador = Cipher
                        .getInstance("AES/ECB/PKCS5Padding");
                cifrador.init(Cipher.ENCRYPT_MODE, chave);
                bytesMensagemCifrada = cifrador.doFinal(textoAberto.getBytes());
                mensagemCifrada = Base64
                        .getEncoder()
                        .encodeToString(bytesMensagemCifrada);
                //System.out.println(">> Mensagem cifrada = "
                //        + mensagemCifrada);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            return mensagemCifrada;
        }

        public String decifrar(String textoCifrado) {
            String mensagem = null;
            // Decriptação
            byte[] bytesMensagemCifrada = Base64
                    .getDecoder()
                    .decode(textoCifrado);
            Cipher decriptador;
            try {
                decriptador = Cipher.getInstance("AES/ECB/PKCS5Padding");
                decriptador.init(Cipher.DECRYPT_MODE, chave);
                byte[] bytesMensagemDecifrada = decriptador.doFinal(bytesMensagemCifrada);
                mensagem = new String(bytesMensagemDecifrada);
                /*
                 * System.out.println("<< Mensagem decifrada = "
                 * + mensagemDecifrada);
                 */
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
            return mensagem;
        }

        //NOVO
        public void setChave(SecretKey chave) {
            this.chave = chave;
        }
        //NOVO
        public SecretKey getChave() {
            return this.chave;
        }
    }
