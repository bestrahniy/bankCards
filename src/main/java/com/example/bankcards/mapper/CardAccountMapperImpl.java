package com.example.bankcards.mapper;

import java.time.Instant;
import org.springframework.stereotype.Component;
import com.example.bankcards.mapper.interfaces.specializedInterface.CardAccountMapper;
import com.example.bankcards.model.entity.CardAccountEntity;

@Component
public class CardAccountMapperImpl implements CardAccountMapper {

    @Override
    public CardAccountEntity toEntity() {
        return CardAccountEntity.builder()
            .currentBalance(0.00)
            .updatedAt(Instant.now())
            .build();
    }

}
