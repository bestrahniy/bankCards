package com.example.bankcards.mapper;

import java.time.Instant;
import org.springframework.stereotype.Component;
import com.example.bankcards.mapper.interfaces.specializedInterface.CardAccountMapper;
import com.example.bankcards.model.entity.CardAccountEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper for CardAccountEntity creation.
 * Creates new card account entities with default initial balance.
 */
@Component
@Slf4j
public class CardAccountMapperImpl implements CardAccountMapper {

    /**
     * Creates a new CardAccountEntity with default values.
     * Sets initial balance to 0.00 and current timestamp.
     *
     * @return new card account entity
     */
    @Override
    public CardAccountEntity toEntity() {
        log.debug("Creating new CardAccountEntity");
        
        Instant now = Instant.now();
        CardAccountEntity entity = CardAccountEntity.builder()
            .currentBalance(0.00)
            .updatedAt(now)
            .build();
        
        log.trace("Card account created with timestamp: {}", now);
        return entity;
    }

}