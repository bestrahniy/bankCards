package com.example.bankcards.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardResponse {

    private String login;

    private String cardNumber;

    private Instant expiredAt;

}
