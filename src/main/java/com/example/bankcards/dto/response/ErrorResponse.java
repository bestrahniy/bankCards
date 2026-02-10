package com.example.bankcards.dto.response;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response format for API errors
 * 
 * Returned for all 4xx and 5xx HTTP status codes
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
public class ErrorResponse {

    /**
     * Error occurrence timestamp in server local time.
     */
    @Schema(
        description = "Error timestamp",
        example = "2024-01-15T14:30:00.123",
        format = "date-time"
    )
    private LocalDateTime timestamp;

    /**
     * HTTP status code of the error.
     */
    @Schema(
        description = "HTTP status code",
        example = "400"
    )
    private int status;

    /**
     * HTTP status text description.
     */
    @Schema(
        description = "HTTP status text",
        example = "Bad Request"
    )
    private String error;

    /**
     * Human-readable error message with details.
     */
    @Schema(
        description = "Detailed error message",
        example = "Card number must be 16 digits"
    )
    private String message;

}