package com.example.bankcards.util;

import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.repository.BankCardsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardAccessValidator {

    private final BankCardsRepository bankCardsRepository;

    private final AesEncryption aesEncryption;

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
        return bankCard.getExpiresAt().isAfter(Instant.now());
    }

}
