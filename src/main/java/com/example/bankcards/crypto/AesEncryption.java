package com.example.bankcards.crypto;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.bankcards.exception.encryprionExceprion.EncryptionException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * AES encryption service for protecting sensitive card data
 * Uses AES-GCM algorithm for secure encryption and decryption
 */
@Component
@Slf4j
public class AesEncryption {

    @Value("${encryption.aes.secret-key}")
    private String encryptionKey;

    @Value("${encryption.aes.iv-length}")
    private Integer IV_LENGTH;

    private SecretKey secretKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final String KEY_ALGORITHM = "AES";

    private static final int TAG_LENGTH = 128;

    /**
     * Encrypts a payment card number using AES-GCM
     * Derives a unique initialization vector from the card number
     * combined with the secret key, ensuring identical card numbers
     * produce different ciphertexts when encrypted multiple times
     *
     * @param cardNumber card number to encrypt
     * @return Base64-encoded string containing IV and ciphertext
     */
    public String encrypt(String cardNumber) {
        log.debug("Starting encryption");
        try {
            log.trace("Deriving IV from card number");
            byte[] iv = deriveIVFromCardNumber(cardNumber);
            
            log.trace("Initializing cipher with IV length: {}", iv.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            log.trace("Encrypting plaintext ({} bytes)...", cardNumber.getBytes("UTF-8").length);
            byte[] ciphertext = cipher.doFinal(cardNumber.getBytes("UTF-8"));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            log.info("Encryption has end");
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception exception) {
            log.error("Encryption failed for card");
            throw new EncryptionException("Failed to encrypt card number");
        }
    }

    /**
     * Decrypts encrypted card number back to original
     * Reverses the encryption process by extracting the IV from the
     * beginning of the Base64-decoded data and using it with the secret key
     * to decrypt the remaining ciphertext
     * 
     * @param hash Base64-encoded encrypted data containing IV and ciphertext
     * @return card number
     */
    public String decrypt(String hash) {
        log.debug("Starting decryption");
        try {
            log.trace("Base64 decoding");
            byte[] decoded = Base64.getDecoder().decode(hash);
            
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);
            
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);
            
            log.trace("Decrypting with IV length: {}, ciphertext length: {}", 
                iv.length, ciphertext.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            log.debug("Decryption successful");
            return new String(plaintext, "UTF-8");
            
        } catch (Exception exception) {
            log.error("Decryption failed");
            throw new EncryptionException("Failed to decrypt card number");
        }
    }

    /**
     * Initializes the AES secret key after bean construction
     */
    @PostConstruct
    private void generateKey() {
        log.info("Initializing AES encryption with algorithm: {}", ALGORITHM);
        log.debug("Configured IV length: {} bytes", IV_LENGTH);
        
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
            log.trace("Key decoded successfully, length: {} bytes", decodedKey.length);
            
            if (decodedKey.length != 32) {
                log.error("Invalid AES key length: {} bytes (expected 32 bytes for AES-256)",
                    decodedKey.length);
                throw new IllegalArgumentException("AES-256 key must be 32 bytes");
            }
            
            secretKey = new SecretKeySpec(decodedKey, KEY_ALGORITHM);
            log.info("AES encryption initialized successfully with {}-bit key",
                decodedKey.length * 8);
            
        } catch (Exception exception) {
            log.error("Failed to initialize AES encryption", exception);
            throw new IllegalStateException("AES encryption initialization failed", exception);
        }
    }

    /**
     * Derives a deterministic initialization vector from a card number
     * @param cardNumber The card number to derive IV from
     * @return Byte array of length IV_LENGTH suitable for AES-GCM IV
     */
    private byte[] deriveIVFromCardNumber(String cardNumber) {
        log.trace("Deriving IV from card number");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((cardNumber + encryptionKey).getBytes("UTF-8"));
            byte[] iv = Arrays.copyOf(hash, IV_LENGTH);
            
            log.trace("IV derived successfully: {} bytes", iv.length);
            return iv;
        } catch (Exception exception) {
            log.error("Failed to derive IV for card");
            throw new EncryptionException("Failed to derive IV");
        }
    }


}
