package com.example.bankcards.mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

import com.example.bankcards.crypto.AesEncryption;
import com.example.bankcards.crypto.AesHelper;
import com.example.bankcards.dto.response.CardActiveStatusResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.mapper.abstractClass.AbstractEncryptionMapper;
import com.example.bankcards.mapper.interfaces.specializedInterface.BankCardMapper;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.PaymentTransactionsEntity;

@Component
public class BankCardMapperImpl extends AbstractEncryptionMapper implements BankCardMapper {

    public BankCardMapperImpl(AesEncryption aesEncryption, AesHelper aesHelper) {
        super(aesEncryption, aesHelper);
    }

    @Override
    public BankCardsEntity toEntity(CardAccountEntity cardAccount) {
        Instant now = Instant.now();

        return BankCardsEntity.builder()
            .number(encrypt(generateCardNumber()))
            .cvc2(ThreadLocalRandom.current().nextInt(100, 999))
            .createdAt(now)
            .expiresAt(now.plus(5 * 365, ChronoUnit.DAYS))
            .cardAccountEntity(cardAccount)
            .build();
    }

    @Override
    public CreateCardResponse toDtoCreateCardResponse(BankCardsEntity bankCardsEntity) {
        return CreateCardResponse.builder()
            .login(bankCardsEntity.getUser().getLogin())
            .numberCard(getMaskedCardNumber(bankCardsEntity.getNumber()))
            .build();
    }

    @Override
    public CardResponse toDtoCardResponse(BankCardsEntity bankCardsEntity) {
        return CardResponse.builder()
            .login(bankCardsEntity.getUser().getLogin())
            .cardNumber(getMaskedCardNumber(bankCardsEntity.getNumber()))
            .expiredAt(bankCardsEntity.getExpiresAt())
            .build();
    }

    @Override
    public CardStatusResponse toDtoCardStatusResponse(BankCardsEntity bankCardsEntity) {
        CardAccountEntity cardAccountEntity = bankCardsEntity.getCardAccountEntity();

        return CardStatusResponse.builder()
            .currentBalance(cardAccountEntity.getCurrentBalance())
            .paymentTransaction(
                cardAccountEntity.getPaymentTransactionsEntities().stream()
                    .map(this::mapToPaymentTransaction)
                    .toList()
            )
            .build();
    }

    @Override
    public CardActiveStatusResponse toDtoCardActiveStatusResponse(BankCardsEntity bankCardsEntity) {
        return CardActiveStatusResponse.builder()
            .cardNumber(getMaskedCardNumber(bankCardsEntity.getNumber()))
            .isActive(bankCardsEntity.getIsActive())
            .build();
    }

    private CardStatusResponse.PaymentTransaction mapToPaymentTransaction(PaymentTransactionsEntity paymentTransaction) {
    return CardStatusResponse.PaymentTransaction.builder()
        .senderCardId(paymentTransaction.getSenderCardAccountId().getId())
        .recipientCardId(paymentTransaction.getRecipientAccountId())
        .comment(paymentTransaction.getComment())
        .amount(paymentTransaction.getAmount())
        .type(paymentTransaction.getTransactionType().getTransactionsType().toString())
        .status(paymentTransaction.getTransactionsStatus().getTransactionsStatus().toString())
        .createdAt(paymentTransaction.getCreatedAt())
        .build();
    }

}
