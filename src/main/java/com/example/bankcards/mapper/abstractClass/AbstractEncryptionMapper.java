package com.example.bankcards.mapper.abstractClass;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.example.bankcards.crypto.AesEncryption;
import com.example.bankcards.crypto.AesHelper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public abstract class AbstractEncryptionMapper {

    private final AesEncryption aesEncryption;

    private final AesHelper aesHelper;

    protected String encrypt(String value) {
        return aesEncryption.encrypt(value);
    }

    protected String getMaskedCardNumber(String encryptedNumber) {
        return aesHelper.getMaskedCardNumber(encryptedNumber);
    }

    protected String generateCardNumber() {
        long cardNumberLong = ThreadLocalRandom.current()
            .nextLong(1000_0000_0000_0000L, 9999_9999_9999_9999L);
        return String.format("%016d", cardNumberLong);
    }

}
