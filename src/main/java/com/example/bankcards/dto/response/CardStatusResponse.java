package com.example.bankcards.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CardStatusResponse {

    private Double currentBalance;

    private List<PaymentTransaction> paymentTransaction;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class PaymentTransaction {

        private UUID senderCardId;

        private UUID recipientCardId;

        private String comment;

        private Double amount;

        private String type;

        private String status;

        private Instant createdAt;
    }

}
