package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Request containing a payment card number.
 * Used for card-specific operations like balance check or blocking.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Card number request for card operations")
public class CardNumberRequest {

    /**
     * Payment card number without spaces or hyphens.
     * Must be a valid 16-digit card number owned by the user.
     * Internally encrypted before processing.
     * 
     * Example: "4111111111111111"
     */
    @NonNull
    @Schema(
        description = "16-digit payment card number",
        example = "4111111111111111",
        required = true,
        pattern = "^\\d{16}$"
    )
    private String cardNumber;

}