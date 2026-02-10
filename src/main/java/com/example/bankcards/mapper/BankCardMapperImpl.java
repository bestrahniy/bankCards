package com.example.bankcards.mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper for BankCardsEntity transformations and DTO conversions.
 * 
 * Handles mapping between BankCardsEntity and various card-related DTOs
 */
@Component
@Slf4j
public class BankCardMapperImpl extends AbstractEncryptionMapper implements BankCardMapper {

    public BankCardMapperImpl(AesEncryption aesEncryption, AesHelper aesHelper) {
        super(aesEncryption, aesHelper);
    }

    /**
     * Creates a new BankCardsEntity with generated card details.
     * 
     * Generates random card number and CVV, sets expiration to 5 years from now.
     * Card number is immediately encrypted before storage.
     * 
     * @param cardAccount associated card account entity
     * @return new bank card entity ready for persistence
     */
    @Override
    public BankCardsEntity toEntity(CardAccountEntity cardAccount) {
        log.info("Creating new BankCardsEntity for account ID: {}", 
            cardAccount != null ? cardAccount.getId() : "null");
        
        Instant now = Instant.now();
        log.debug("Setting card creation time: {}", now);

        BankCardsEntity entity = BankCardsEntity.builder()
            .number(encrypt(generateCardNumber()))
            .cvc2(ThreadLocalRandom.current().nextInt(100, 999))
            .createdAt(now)
            .expiresAt(now.plus(5 * 365, ChronoUnit.DAYS))
            .cardAccountEntity(cardAccount)
            .build();
        
        log.info("Bank card entity created with expiration: {}", entity.getExpiresAt());
        return entity;
    }

    /**
     * Maps BankCardsEntity to CreateCardResponse DTO.
     * 
     * Includes masked card number for user display and card owner login.
     * 
     * @param bankCardsEntity source bank card entity
     * @return card creation response DTO
     */
    @Override
    public CreateCardResponse toDtoCreateCardResponse(BankCardsEntity bankCardsEntity) {
        log.debug("Mapping BankCardsEntity to CreateCardResponse");
        
        String maskedNumber = getMaskedCardNumber(bankCardsEntity.getNumber());
        String login = bankCardsEntity.getUser() != null ? bankCardsEntity.getUser().getLogin() : "null";
        
        log.trace("Card masked: {}, User login: {}", maskedNumber, login);
        
        return CreateCardResponse.builder()
            .login(login)
            .numberCard(maskedNumber)
            .build();
    }

    /**
     * Maps BankCardsEntity to CardResponse DTO.
     * Provides basic card information including masked number and expiration.
     * 
     * @param bankCardsEntity source bank card entity
     * @return card response DTO
     */
    @Override
    public CardResponse toDtoCardResponse(BankCardsEntity bankCardsEntity) {
        log.debug("Mapping BankCardsEntity to CardResponse");
        
        return CardResponse.builder()
            .login(bankCardsEntity.getUser().getLogin())
            .cardNumber(getMaskedCardNumber(bankCardsEntity.getNumber()))
            .expiredAt(bankCardsEntity.getExpiresAt())
            .build();
    }

    /**
     * Maps BankCardsEntity to CardStatusResponse DTO with balance and transactions.
     * Includes current balance and recent payment transactions.
     * 
     * @param bankCardsEntity source bank card entity
     * @return card status response DTO with full financial details
     */
    @Override
    public CardStatusResponse toDtoCardStatusResponse(BankCardsEntity bankCardsEntity) {
        log.debug("Mapping BankCardsEntity to CardStatusResponse");
        
        CardAccountEntity cardAccountEntity = bankCardsEntity.getCardAccountEntity();
        log.trace("Card account ID: {}, Balance: {}",
            cardAccountEntity.getId(), cardAccountEntity.getCurrentBalance());

        List<CardStatusResponse.PaymentTransaction> transactions =
            cardAccountEntity.getPaymentTransactionsEntities().stream()
                .map(this::mapToPaymentTransaction)
                .toList();
        
        log.trace("Mapped {} payment transactions", transactions.size());

        return CardStatusResponse.builder()
            .currentBalance(cardAccountEntity.getCurrentBalance())
            .paymentTransaction(transactions)
            .build();
    }

    /**
     * Maps BankCardsEntity to CardActiveStatusResponse DTO.
     * Provides card activation status with masked card number.
     * 
     * @param bankCardsEntity source bank card entity
     * @return card active status response DTO
     */
    @Override
    public CardActiveStatusResponse toDtoCardActiveStatusResponse(BankCardsEntity bankCardsEntity) {
        log.debug("Mapping BankCardsEntity to CardActiveStatusResponse");
        
        return CardActiveStatusResponse.builder()
            .cardNumber(getMaskedCardNumber(bankCardsEntity.getNumber()))
            .isActive(bankCardsEntity.getIsActive())
            .build();
    }

    /**
     * Maps PaymentTransactionsEntity to PaymentTransaction DTO.
     * Converts internal transaction entity to API response format.
     * 
     * @param paymentTransaction source payment transaction entity
     * @return payment transaction DTO
     */
    private CardStatusResponse.PaymentTransaction mapToPaymentTransaction(PaymentTransactionsEntity paymentTransaction) {
        log.trace("Mapping PaymentTransactionsEntity to PaymentTransaction DTO");
        
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