package com.example.bankcards.mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.PrimitiveIterator;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.util.AesEncryption;
import com.example.bankcards.util.AesHelper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BankCardMapper {

    private final AesEncryption aesEncryption;

    private final AesHelper aesHelper;

    public BankCardsEntity toEntity(CardAccountEntity cardAccount) {
        Instant now = Instant.now();

        return BankCardsEntity.builder()
            .number(aesEncryption.encrypt(
                    String.valueOf(ThreadLocalRandom.current()
                    .nextLong(1000_0000_0000_0000L, 9999_9999_9999_9999L)))
                )
            .cvc2(ThreadLocalRandom.current().nextInt(100, 999))
            .createdAt(now)
            .expiresAt(now.plus(5 * 365, ChronoUnit.DAYS))
            .cardAccountEntity(cardAccount)
            .build();
    }

    public CreateCardResponse toDto(BankCardsEntity bankCardsEntity) {
        return CreateCardResponse.builder()
            .login(bankCardsEntity.getUser().getLogin())
            .numberCard(aesHelper.getMaskedCardNumber(
                bankCardsEntity.getNumber().toString()))
            .build();
    }

}
