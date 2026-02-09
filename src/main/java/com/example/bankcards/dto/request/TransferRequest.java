package com.example.bankcards.dto.request;

import com.example.bankcards.model.enums.TransactionsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TransferRequest {

    @NonNull
    private String fromNumberCard;

    @NonNull
    private String toNumberCard;

    @NonNull
    private Double amount;

    private String comment;

    private TransactionsType transactionsType;

}
