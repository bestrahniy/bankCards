package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response containing card activation status.
 * 
 * Returned by card status check endpoints to indicate
 * whether a card is active or blocked.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Card activation status response")
public class CardActiveStatusResponse {

    /**
     * Masked card number for display (PCI-DSS compliant).
     * Shows only last 4 digits for security.
     * 
     * Example: "**** **** **** 5678"
     */
    @Schema(
        description = "Masked card number showing last 4 digits",
        example = "**** **** **** 5678"
    )
    private String cardNumber;

    /**
     * Card activation status.
     * true = card is active and can be used for transactions.
     * false = card is blocked/suspended.
     */
    @Schema(
        description = "Card activation status",
        example = "true"
    )
    private Boolean isActive;

}