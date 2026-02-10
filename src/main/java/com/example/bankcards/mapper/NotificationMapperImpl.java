package com.example.bankcards.mapper;

import org.springframework.stereotype.Component;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.mapper.interfaces.specializedInterface.NotificationMapper;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.EventType;

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper for NotificationEntity transformations and DTO conversions.
 * Handles creation of notification entities and mapping to response DTOs.
 */
@Component
@Slf4j
public class NotificationMapperImpl implements NotificationMapper {

    /**
     * Creates a notification entity for card creation events.
     * 
     * @param user user requesting card creation
     * @return notification entity for CREATE_CARD event
     */
    @Override
    public NotificationEntity toEntity(UsersEntity user) {
        log.debug("Creating CREATE_CARD notification for user: {}",
            user != null ? user.getLogin() : "null");
        
        NotificationEntity entity = NotificationEntity.builder()
            .event(EventType.CREATE_CARD)
            .user(user)
            .card(null)
            .build();
        
        log.trace("Notification entity created with event: CREATE_CARD");
        return entity;
    }

    /**
     * Creates a notification entity for card block events.
     * 
     * @param user user requesting card block
     * @param cardAccount card account being blocked
     * @return notification entity for BLOCK_CARD event
     */
    @Override
    public NotificationEntity toEntity(UsersEntity user, CardAccountEntity cardAccount) {
        log.debug("Creating BLOCK_CARD notification for user: {}, card account: {}", 
            user != null ? user.getLogin() : "null",
            cardAccount != null ? cardAccount.getId() : "null");
        
        NotificationEntity entity = NotificationEntity.builder()
            .event(EventType.BLOCK_CARD)
            .user(user)
            .card(cardAccount)
            .build();
        
        log.trace("Notification entity created with event: BLOCK_CARD");
        return entity;
    }

    /**
     * Maps notification entity to NotificationResponse DTO.
     * 
     * @param user notification recipient user
     * @param notification source notification entity
     * @return notification response DTO
     */
    @Override
    public NotificationResponse toDto(UsersEntity user, NotificationEntity notification) {
        log.debug("Mapping NotificationEntity to NotificationResponse");
        
        return NotificationResponse.builder()
            .login(user.getLogin())
            .notification(NotificationResponse.NotificationDto.builder()
                .id(notification.getId())
                .event(notification.getEvent().toString())
                .createdAt(notification.getCreatedAt())
                .isActive(notification.isActive())
                .build())
            .build();
    }

}