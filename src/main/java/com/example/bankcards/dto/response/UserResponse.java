package com.example.bankcards.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Complete user information response.
 * 
 * Returned by user profile endpoints with full user details
 * including authentication tokens when relevant.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Complete user information response")
public class UserResponse {

    /**
     * Unique user identifier (UUID v4).
     */
    @NonNull
    @Schema(
        description = "User unique identifier",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID id;

    /**
     * Username for display and login.
     */
    @NonNull
    @Schema(
        description = "Username",
        example = "john_doe"
    )
    private String login;

    /**
     * User's email address.
     */
    @NonNull
    @Schema(
        description = "User email address",
        example = "john@example.com",
        format = "email"
    )
    private String email;

    /**
     * Account creation timestamp in UTC.
     * 
     * Format: ISO 8601 with milliseconds
     */
    @NonNull
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            timezone = "UTC")
    @Schema(
        description = "Account creation timestamp in UTC",
        example = "2024-01-15T10:30:00.123Z",
        format = "date-time"
    )
    private Instant createdAt;

    /**
     * Set of user roles determining permissions.
     * Always contains at least "USER" role.
     */
    @NonNull
    @Schema(
        description = "User roles/permissions",
        example = "[\"USER\", \"ADMIN\"]"
    )
    private Set<String> roles;

    /**
     * JSON Web Token for authenticated API requests.
     * Included only during login/registration/refresh.
     */
    @Schema(
        description = "JWT access token (included in auth responses)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String jwt;

    /**
     * Refresh token for obtaining new JWT.
     * Included only during login/registration/refresh.
     */
    @Schema(
        description = "Refresh token (included in auth responses)",
        example = "dG9rZW4tcmVmcmVzaC12YWx1ZQ=="
    )
    private String refreshToken;

}