package com.example.bankcards.dto.response;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for completed transaction creation.
 * 
 * Returned after successful money transfer or payment.
 * Contains transaction identifiers and details for reference.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Transaction creation response")
public class CreateTransactionResponse {

    /**
     * Unique identifier of the sender's card account.
     */
    @Schema(
        description = "Sender card account ID",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID fromCardAccountId;

    /**
     * Unique identifier of the recipient's card account.
     */
    @Schema(
        description = "Recipient card account ID",
        example = "550e8400-e29b-41d4-a716-446655440001"
    )
    private UUID toCardAccountId;

    /**
     * Type of transaction that was created.
     */
    @Schema(
        description = "Transaction type",
        example = "TRANSFER",
        allowableValues = {"TRANSFER", "PAYMENT", "DEPOSIT", "WITHDRAWAL", "INTERNAL_TRANSFER"}
    )
    private String transactionType;

    /**
     * Transaction amount in currency units.
     */
    @Schema(
        description = "Transaction amount",
        example = "1500.50"
    )
    private Double amount;

}