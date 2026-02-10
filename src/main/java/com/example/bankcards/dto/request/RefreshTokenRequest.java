package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to refresh authentication token using refresh token.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Refresh token request for obtaining new access token")
public class RefreshTokenRequest {

    /**
     * Hashed refresh token obtained during previous authentication.
     * Must be valid and not expired to issue new access token.
     */
    @Schema(
        description = "Hashed refresh token for obtaining new access token",
        example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    )
    private String hashRefreshToken;

}