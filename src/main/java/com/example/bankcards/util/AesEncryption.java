package com.example.bankcards.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.bankcards.exception.EncryptionException;
import jakarta.annotation.PostConstruct;

@Component
public class AesEncryption {

    @Value("${encryption.aes.secret-key}")
    private String encryptionKey;

    @Value("${encryption.aes.iv-length}")
    private Integer IV_LENGTH;

    private SecretKey secretKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final String KEY_ALGORITHM = "AES";

    private static final int TAG_LENGTH = 128;

    public String encrypt(String cardNumber) {
        try {
            byte[] iv = generateSecureRandomBytes(IV_LENGTH);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            byte[] ciphertext = cipher.doFinal(cardNumber.getBytes("UTF-8"));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            return Base64.getEncoder().encodeToString(byteBuffer.array());
            
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt card number");
        }
    }

    public String decrypt(String hash) {
        try {
            byte[] decoded = Base64.getDecoder().decode(hash);
            
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);
            
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, "UTF-8");
            
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt card number");
        }
    }

    @PostConstruct
    private void generateKey() {
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
        if (decodedKey.length != 32) {
            throw new IllegalArgumentException("AES-256 key must be 32 bytes");
        }
        secretKey = new SecretKeySpec(decodedKey, KEY_ALGORITHM);
    }

    private byte[] generateSecureRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

}
