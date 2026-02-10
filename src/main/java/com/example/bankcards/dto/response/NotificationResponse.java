package com.example.bankcards.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Notification response containing user notification details.
 * 
 * Returned by notification endpoints to display system
 * alerts and messages to users.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "User notification response")
public class NotificationResponse {

    /**
     * Username of the notification recipient.
     */
    @Schema(
        description = "Notification recipient username",
        example = "john_doe"
    )
    private String login;

    /**
     * Notification details and content.
     */
    @Schema(description = "Notification details")
    private NotificationDto notification;

    /**
     * Notification data transfer object.
     * Contains specific notification information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Notification details")
    public static class NotificationDto {

        /**
         * Unique notification identifier.
         */
        @Schema(
            description = "Notification ID",
            example = "12345"
        )
        private Long id;

        /**
         * Notification event type.
         * 
         * Possible values: CREATE_CARD, BLOCK_CARD
         */
        @Schema(
            description = "Notification event type",
            example = "CARD_BLOCK_REQUEST",
            allowableValues = {"CREATE_CARD", "BLOCK_CARD"}
        )
        private String event;

        /**
         * Masked card number related to the notification.
         */
        @Schema(
            description = "Related card number (masked)",
            example = "**** **** **** 5678"
        )
        private String cardNumber;

        /**
         * Notification creation timestamp in UTC.
         */
        @Schema(
            description = "Notification creation time",
            example = "2024-01-15T14:30:00Z",
            format = "date-time"
        )
        private Instant createdAt;

        /**
         * Notification activity status.
         * true = notification is active and visible to user.
         * false = notification has been read or archived.
         */
        @Schema(
            description = "Notification active status",
            example = "true"
        )
        private Boolean isActive;

    }

}
