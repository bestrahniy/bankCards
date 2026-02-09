package com.example.bankcards.util;

import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.example.bankcards.exception.BankCardNotFoundException;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.repository.BankCardsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardAccessValidator {

    private final BankCardsRepository bankCardsRepository;

    private final AesEncryption aesEncryption;

    public BankCardsEntity findBankCardByNumber(String cardNumber) {
        String encryptedCardNumber = aesEncryption.encrypt(cardNumber);
        return bankCardsRepository.findByNumber(encryptedCardNumber)
            .orElseThrow(() -> new BankCardNotFoundException(cardNumber));
    }

    public Boolean checkCard(String currentNumber) throws IllegalAccessException {
        BankCardsEntity bankCard = findBankCardByNumber(currentNumber);
        return checkActiveCard(bankCard) && checkExpiresCard(bankCard);
    }

    public Boolean checkActiveCard(BankCardsEntity bankCard) {
        return bankCard.getIsActive();
    }

    public Boolean checkExpiresCard(BankCardsEntity bankCard) {
        return bankCard.getExpiresAt().isAfter(Instant.now());
    }

}
