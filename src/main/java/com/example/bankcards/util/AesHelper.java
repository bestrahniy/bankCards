package com.example.bankcards.util;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AesHelper {

    private final AesEncryption aesEncryption;

    public String getMaskedCardNumber(String encryptedData) {
        String fullNumber = aesEncryption.decrypt(encryptedData);
        if (fullNumber.length() >= 4) {
            String lastFour = fullNumber.substring(fullNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        return "****";
    }

}
