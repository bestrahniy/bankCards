package com.example.bankcards.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basic card information response.
 * 
 * Returned by card listing endpoints with essential
 * card details for display purposes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Basic card information response")
public class CardResponse {

    /**
     * Card owner's username.
     */
    @Schema(
        description = "Card owner username",
        example = "john_doe"
    )
    private String login;

    /**
     * Masked card number for secure display.
     */
    @Schema(
        description = "Masked card number",
        example = "**** **** **** 5678"
    )
    private String cardNumber;

    /**
     * Card expiration date and time in UTC.
     * After this date, card cannot be used for transactions.
     * 
     * Format: ISO 8601 (YYYY-MM-DDTHH:MM:SSZ)
     */
    @Schema(
        description = "Card expiration timestamp in UTC",
        example = "2026-12-31T23:59:59Z",
        format = "date-time"
    )
    private Instant expiredAt;

}