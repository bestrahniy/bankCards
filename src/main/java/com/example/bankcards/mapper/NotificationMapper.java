package com.example.bankcards.mapper;

import org.springframework.stereotype.Component;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.EventType;

@Component
public class NotificationMapper {

    public NotificationEntity toEntity(UsersEntity user) {
        return NotificationEntity.builder()
            .event(EventType.CREATE_CARD)
            .user(user)
            .card(null)
            .build();
    }

    public NotificationEntity toEntity(UsersEntity user, CardAccountEntity cardAccount) {
        return NotificationEntity.builder()
            .event(EventType.BLOCK_CARD)
            .user(user)
            .card(cardAccount)
            .build();
    }

    public NotificationResponse toDto(UsersEntity user, NotificationEntity notification) {
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
