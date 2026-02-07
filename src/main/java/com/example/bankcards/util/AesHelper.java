package com.example.bankcards.util;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.repository.BankCardsRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AesHelper {

    private final AesEncryption aesEncryption;

    private final BankCardsRepository bankCardsRepository;

    public Boolean checkCard(String currentNumber) throws IllegalAccessException {
        String currentHash = aesEncryption.encrypt(currentNumber);

        BankCardsEntity bankCard = bankCardsRepository.findByNumber(currentHash)
            .orElseThrow(() -> new IllegalAccessException("bank card is not found"));

        return checkActiveCard(bankCard) && checkExpiresCard(bankCard);
    }

    public Boolean checkActiveCard(BankCardsEntity bankCard) {
        return bankCard.getIsActive();
    }

    public Boolean checkExpiresCard(BankCardsEntity bankCard) {
        return Date.from(bankCard.getExpiresAt()).before(new Date());
    }

    public String getMaskedCardNumber(String encryptedData) {
        String fullNumber = aesEncryption.decrypt(encryptedData);
        if (fullNumber.length() >= 4) {
            String lastFour = fullNumber.substring(fullNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        return "****";
    }

}
