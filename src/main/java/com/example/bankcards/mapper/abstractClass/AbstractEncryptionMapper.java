package com.example.bankcards.mapper.abstractClass;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;
import com.example.bankcards.crypto.AesEncryption;
import com.example.bankcards.crypto.AesHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base mapper providing common encryption and card number utilities.
 * Contains functionality for mappers that work with sensitive card data
 * Uses AES encryption for secure data handling and follows PCI-DSS standards
 * for card number masking in display outputs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractEncryptionMapper {

    private final AesEncryption aesEncryption;

    private final AesHelper aesHelper;

    /**
     * Encrypts sensitive card number data for secure storage.
     * 
     * @param value plaintext card number to encrypt
     * @return encrypted card number as Base64 string
     */
    protected String encrypt(String value) {
        log.trace("Encrypting value (length: {} chars)", value != null ? value.length() : 0);
        String encrypted = aesEncryption.encrypt(value);
        log.trace("Encryption completed");
        return encrypted;
    }

    /**
     * Creates masked card number for display.
     * 
     * @param encryptedNumber encrypted card number from database
     * @return masked card number in format "**** **** **** 1234"
     */
    protected String getMaskedCardNumber(String encryptedNumber) {
        log.trace("Masking encrypted card number");
        String masked = aesHelper.getMaskedCardNumber(encryptedNumber);
        log.trace("Card masked: {}", masked);
        return masked;
    }

    /**
     * Generates a random 16-digit card number for new cards.
     * 
     * Uses ThreadLocalRandom for thread-safe random generation.
     * Ensures generated number is within valid card number ranges.
     * 
     * @return 16-digit card number as string
     */
    protected String generateCardNumber() {
        log.debug("Generating new card number");
        long cardNumberLong = ThreadLocalRandom.current()
            .nextLong(1000_0000_0000_0000L, 9999_9999_9999_9999L);
        String cardNumber = String.format("%016d", cardNumberLong);
        log.debug("Generated card number: **** **** **** {}", cardNumber.substring(12));
        return cardNumber;
    }

}