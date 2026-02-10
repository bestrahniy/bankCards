package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User activation status response.
 * 
 * Returned by user status check endpoints to indicate
 * whether a user account is active or suspended.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User activation status response")
public class UserActiveResponse {

    /**
     * Username of the user.
     */
    @Schema(
        description = "Username",
        example = "john_doe"
    )
    private String login;

    /**
     * User account activation status.
     * true = account is active and can authenticate.
     * false = account is suspended/blocked.
     */
    @Schema(
        description = "User account active status",
        example = "true"
    )
    private Boolean isActive;

}
