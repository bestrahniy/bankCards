package com.example.bankcards.mapper;

import java.security.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.exception.transactionException.TransactionStatusNotFoundException;
import com.example.bankcards.exception.transactionException.TransactionTypeNotFoundException;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.PaymentTransactionsEntity;
import com.example.bankcards.model.enums.TransactionsStatusType;
import com.example.bankcards.repository.TransactionTypeRepository;
import com.example.bankcards.repository.TransactionsStatusRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentTransactionsMapper {

    private final TransactionTypeRepository transactionTypeRepository;

    private final TransactionsStatusRepository transactionsStatusRepository;

    public PaymentTransactionsEntity toEntity(CardAccountEntity cardAccountEntity,
        UUID recipientAccountId, TransferRequest transferRequest) {
        return PaymentTransactionsEntity.builder()
            .senderCardAccountId(cardAccountEntity)
            .recipientAccountId(recipientAccountId)
            .comment(transferRequest.getComment())
            .amount(transferRequest.getAmount())
            .transactionType(transactionTypeRepository
                .findByTransactionsType(transferRequest.getTransactionsType())
                    .orElseThrow(() -> new TransactionTypeNotFoundException("transaction type has not found")))
            .transactionsStatus(transactionsStatusRepository
                .findByTransactionsStatus(TransactionsStatusType.COMPLETED)
                    .orElseThrow(() -> new TransactionStatusNotFoundException("transaction status has not found")))
            .createdAt(Instant.now())
            .build();
    }

    public CreateTransactionResponse toDto(PaymentTransactionsEntity paymentTransactionsEntity) {
        return CreateTransactionResponse.builder()
            .fromCardAccountId(paymentTransactionsEntity.getSenderCardAccountId().getBankCardsEntity().getId())
            .toCardAccountId(paymentTransactionsEntity.getRecipientAccountId())
            .transactionType(paymentTransactionsEntity.getTransactionType().getTransactionsType().toString())
            .amount(paymentTransactionsEntity.getAmount())
            .build();
    }

}
