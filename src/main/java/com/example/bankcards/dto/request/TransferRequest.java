package com.example.bankcards.dto.request;

import com.example.bankcards.model.enums.TransactionsType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Money transfer request between two payment cards.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Money transfer request between cards")
public class TransferRequest {

    /**
     * Source card number for the transfer.
     * Must belong to the authenticated user and be active.
     */
    @NonNull
    @Schema(
        description = "Source card number (sender)",
        example = "1234567812345678",
        required = true
    )
    private String fromNumberCard;

    /**
     * Destination card number for the transfer.
     * Must be a valid and active card in the system.
     */
    @NonNull
    @Schema(
        description = "Destination card number (recipient)",
        example = "8765432187654321",
        required = true
    )
    private String toNumberCard;

    /**
     * Transfer amount in currency units.
     * Must be positive and not exceed sender's balance.
     */
    @NonNull
    @Schema(
        description = "Transfer amount (positive value)",
        example = "1500.50",
        required = true,
        minimum = "0.01"
    )
    private Double amount;

    /**
     * Optional comment for the transaction.
     * Visible to both sender and recipient in transaction history.
     */
    @Schema(
        description = "Optional transaction comment",
        example = "Monthly rent payment"
    )
    private String comment;

    /**
     * Type of transaction being performed.
     * Defaults to TRANSFER if not specified.
     * 
     * Allowed values: TRANSFER, PAYMENT, DEPOSIT, WITHDRAWAL, INTERNAL_TRANSFER
     */
    @Schema(
        description = "Type of transaction",
        example = "TRANSFER",
        allowableValues = {"TRANSFER", "PAYMENT", "DEPOSIT", "WITHDRAWAL", "INTERNAL_TRANSFER"}
    )
    private TransactionsType transactionsType;

}