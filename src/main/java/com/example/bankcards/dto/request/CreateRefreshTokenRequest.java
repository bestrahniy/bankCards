package com.example.bankcards.dto.request;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Request to create a new refresh token in the system.
 * Used internally for token management and rotation.
 * 
 * Note: This is an internal DTO, not exposed via public API.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Request to create refresh token (internal use)")
public class CreateRefreshTokenRequest {

    /**
     * Hashed refresh token value for secure storage.
     * Generated using SHA-256 hash of the original token.
     */
    @NonNull
    @Schema(description = "SHA-256 hashed token value", required = true)
    private String hashToken;

    /**
     * Token creation date.
     * Used to calculate token age and validity.
     */
    @NonNull
    @Schema(description = "Token creation date/time", required = true)
    private Date createdAt;

    /**
     * Token expiration date.
     * After this date, token cannot be used for refresh.
     */
    @NonNull
    @Schema(description = "Token expiration date/time", required = true)
    private Date expiredAt;

}