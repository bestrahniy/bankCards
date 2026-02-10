package com.example.bankcards.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed card status response including balance and transactions.
 * 
 * Returned by card balance check endpoints with full financial
 * overview including current balance and recent transaction history.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Detailed card status with balance and transactions")
public class CardStatusResponse {

    /**
     * Current available balance on the card in currency units.
     * Negative values indicate overdraft/credit.
     */
    @Schema(
        description = "Current card balance",
        example = "1500.50"
    )
    private Double currentBalance;

    /**
     * List of recent payment transactions for this card.
     * Sorted by creation date (newest first).
     * Maximum 50 transactions returned.
     */
    @Schema(
        description = "Recent payment transactions (max 50)"
    )
    private List<PaymentTransaction> paymentTransaction;

    /**
     * Individual payment transaction details.
     * Represents a single money movement to/from the card.
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Payment transaction details")
    public static class PaymentTransaction {

        /**
         * Unique identifier of the sender's card account.
         * null for incoming transactions (when this card is recipient).
         */
        @Schema(
            description = "Sender card account ID",
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        private UUID senderCardId;

        /**
         * Unique identifier of the recipient's card account.
         * null for outgoing transactions (when this card is sender).
         */
        @Schema(
            description = "Recipient card account ID",
            example = "550e8400-e29b-41d4-a716-446655440001"
        )
        private UUID recipientCardId;

        /**
         * Transaction comment provided by sender.
         * Optional field, may be null.
         */
        @Schema(
            description = "Transaction comment",
            example = "Grocery shopping"
        )
        private String comment;

        /**
         * Transaction amount in currency units.
         * Positive for incoming, negative for outgoing transactions.
         */
        @Schema(
            description = "Transaction amount",
            example = "250.75"
        )
        private Double amount;

        /**
         * Type of transaction.
         * 
         * Possible values: TRANSFER, PAYMENT, DEPOSIT, WITHDRAWAL, INTERNAL_TRANSFER
         */
        @Schema(
            description = "Transaction type",
            example = "TRANSFER",
            allowableValues = {"TRANSFER", "PAYMENT", "DEPOSIT", "WITHDRAWAL", "INTERNAL_TRANSFER"}
        )
        private String type;

        /**
         * Transaction processing status.
         * 
         * Possible values: PENDING, COMPLETED, FAILED, FAILED
         */
        @Schema(
            description = "Transaction status",
            example = "COMPLETED",
            allowableValues = {"PENDING", "COMPLETED", "FAILED", "FAILED"}
        )
        private String status;

        /**
         * Transaction creation timestamp in UTC.
         * 
         * Format: ISO 8601 (YYYY-MM-DDTHH:MM:SSZ)
         */
        @Schema(
            description = "Transaction creation timestamp",
            example = "2024-01-15T14:30:00Z",
            format = "date-time"
        )
        private Instant createdAt;
    }

}