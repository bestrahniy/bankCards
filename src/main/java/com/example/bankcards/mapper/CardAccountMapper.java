package com.example.bankcards.mapper;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.example.bankcards.model.entity.CardAccountEntity;

@Component
public class CardAccountMapper {

    public CardAccountEntity toEntity() {
        return CardAccountEntity.builder()
            .currentBalance(0.00)
            .updatedAt(Instant.now())
            .build();
    }
}
