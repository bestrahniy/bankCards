package com.example.bankcards.mapper;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.exception.transactionException.TransactionStatusNotFoundException;
import com.example.bankcards.exception.transactionException.TransactionTypeNotFoundException;
import com.example.bankcards.mapper.interfaces.specializedInterface.PaymentTransactionsMapper;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.PaymentTransactionsEntity;
import com.example.bankcards.model.entity.TransactionTypeEntity;
import com.example.bankcards.model.entity.TransactionsStatusEntity;
import com.example.bankcards.model.enums.TransactionsStatusType;
import com.example.bankcards.repository.TransactionTypeRepository;
import com.example.bankcards.repository.TransactionsStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper for PaymentTransactionsEntity transformations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionsMapperImpl implements PaymentTransactionsMapper {

    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionsStatusRepository transactionsStatusRepository;

    /**
     * Creates PaymentTransactionsEntity from transfer request.
     * 
     * @param cardAccountEntity sender's card account
     * @param recipientAccountId recipient's account ID
     * @param transferRequest transfer request details
     * @return payment transaction entity
     * @throws TransactionTypeNotFoundException if transaction type not found
     * @throws TransactionStatusNotFoundException if transaction status not found
     */
    public PaymentTransactionsEntity toEntity(CardAccountEntity cardAccountEntity,
        UUID recipientAccountId, TransferRequest transferRequest) {
        log.info("Creating PaymentTransactionsEntity for transfer from account: {} to recipient: {}", 
            cardAccountEntity != null ? cardAccountEntity.getId() : "null",
            recipientAccountId);
        
        log.debug("Transfer details - Amount: {}, Type: {}, Comment: {}", 
            transferRequest.getAmount(), 
            transferRequest.getTransactionsType(),
            transferRequest.getComment());

        TransactionTypeEntity transactionType = transactionTypeRepository
            .findByTransactionsType(transferRequest.getTransactionsType())
                .orElseThrow(() -> {
                    log.error("Transaction type not found: {}", transferRequest.getTransactionsType());
                    return new TransactionTypeNotFoundException("transaction type has not found");
                });
        log.trace("Transaction type resolved: {}", transactionType.getTransactionsType());

        TransactionsStatusEntity transactionStatus = transactionsStatusRepository
            .findByTransactionsStatus(TransactionsStatusType.COMPLETED)
                .orElseThrow(() -> {
                    log.error("Transaction status not found: COMPLETED");
                    return new TransactionStatusNotFoundException("transaction status has not found");
                });
        log.trace("Transaction status resolved: COMPLETED");

        PaymentTransactionsEntity entity = PaymentTransactionsEntity.builder()
            .senderCardAccountId(cardAccountEntity)
            .recipientAccountId(recipientAccountId)
            .comment(transferRequest.getComment())
            .amount(transferRequest.getAmount())
            .transactionType(transactionType)
            .transactionsStatus(transactionStatus)
            .createdAt(Instant.now())
            .build();
        
        log.info("Payment transaction entity created with ID: {}", entity.getId());
        return entity;
    }

    /**
     * Maps PaymentTransactionsEntity to CreateTransactionResponse DTO.
     * 
     * @param paymentTransactionsEntity source payment transaction entity
     * @return transaction creation response DTO
     */
    public CreateTransactionResponse toDto(PaymentTransactionsEntity paymentTransactionsEntity) {
        log.debug("Mapping PaymentTransactionsEntity to CreateTransactionResponse");
        
        return CreateTransactionResponse.builder()
            .fromCardAccountId(paymentTransactionsEntity.getSenderCardAccountId().getBankCardsEntity().getId())
            .toCardAccountId(paymentTransactionsEntity.getRecipientAccountId())
            .transactionType(paymentTransactionsEntity.getTransactionType().getTransactionsType().toString())
            .amount(paymentTransactionsEntity.getAmount())
            .build();
    }

}