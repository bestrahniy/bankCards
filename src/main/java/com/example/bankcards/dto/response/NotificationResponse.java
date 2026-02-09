package com.example.bankcards.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotificationResponse {

    private String login;

    private NotificationDto notification;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDto {

        private Long id;

        private String event;

        private String cardNumber;

        private Instant createdAt;

        private Boolean isActive;

    }

}
