package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Authentication request for user login.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Authentication request credentials")
public class AuthorizationRequest {

    /**
     * User's login (username) for authentication.
     * Case-sensitive. Must match registered login exactly.
     */
    @NonNull
    @Schema(description = "User login/username", example = "john_doe", required = true)
    private String login;

    /**
     * User's password for authentication.
     * Will be verified against bcrypt hash in database.
     */
    @NonNull
    @Schema(description = "User password", example = "SecurePass123!", required = true)
    private String password;

}