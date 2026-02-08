package com.example.bankcards.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTransactionResponse {

    private UUID fromCardAccountId;

    private UUID toCardAccountId;

    private String transactionType;

    private Double amount;

}
