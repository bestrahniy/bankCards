package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for successful card creation request.
 * 
 * Returned after a new card request is submitted successfully.
 * Contains basic information about the requested card.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Card creation request response")
public class CreateCardResponse {

    /**
     * Username of the card requester.
     */
    @Schema(
        description = "Card requester username",
        example = "john_doe"
    )
    private String login;

    /**
     * Masked card number for the newly created card.
     * Shows only last 4 digits until full activation.
     */
    @Schema(
        description = "Masked card number of new card",
        example = "**** **** **** 5678"
    )
    private String numberCard;

}
