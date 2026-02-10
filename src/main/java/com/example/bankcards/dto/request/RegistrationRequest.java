package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * User registration request for creating new accounts.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "New user registration request")
public class RegistrationRequest {

    /**
     * Desired username/login for the new account.
     * Must be unique across the system.
     * 
     * Constraints: 5-20 characters, alphanumeric and underscores only.
     */
    @NonNull
    @Size(min = 5, max = 20)
    @Schema(
        description = "Desired username (5-20 characters)",
        example = "john_doe",
        required = true,
        minLength = 5,
        maxLength = 20
    )
    private String login;

    /**
     * Account password.
     * Minimum 8 characters. Recommended to include uppercase,
     * lowercase, numbers, and special characters for security.
     */
    @NonNull
    @Size(min = 8)
    @Schema(
        description = "Account password (minimum 8 characters)",
        example = "SecurePass123!",
        required = true,
        minLength = 8
    )
    private String password;

    /**
     * User's email address.
     * Must be valid email format and unique in the system.
     * Used for notifications and password recovery.
     * Example: "john@example.com"
     */
    @NonNull
    @Email
    @Schema(
        description = "User email address",
        example = "john@example.com",
        required = true,
        format = "email"
    )
    private String email;

}
