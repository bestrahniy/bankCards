package com.example.bankcards.crypto;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * Masks card number for safe logging
 */
@Component
@RequiredArgsConstructor
public class AesHelper {

    private final AesEncryption aesEncryption;

    /**
     * Creates a PCI-DSS compliant masked representation of a card number
     * @param encryptedData hash number card
     * @return masked number with 4 last digits
     */
    public String getMaskedCardNumber(String encryptedData) {
        String fullNumber = aesEncryption.decrypt(encryptedData);
        if (fullNumber.length() >= 4) {
            String lastFour = fullNumber.substring(fullNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        return "****";
    }

}
