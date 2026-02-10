package com.example.bankcards.mapper.interfaces.specializedInterface;

import java.util.UUID;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.PaymentTransactionsEntity;

public interface PaymentTransactionsMapper {
    PaymentTransactionsEntity toEntity(CardAccountEntity cardAccountEntity,
        UUID recipientAccountId, TransferRequest transferRequest);

    CreateTransactionResponse toDto(PaymentTransactionsEntity paymentTransactionsEntity);

}
