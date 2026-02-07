package com.example.bankcards.mapper;

import org.springframework.stereotype.Component;

import com.example.bankcards.dto.response.CreateCardNotificationResponse;
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

    public CreateCardNotificationResponse toDto(UsersEntity user, NotificationEntity notification) {
        return CreateCardNotificationResponse.builder()
            .login(user.getLogin())
            .notification(CreateCardNotificationResponse.NotificationDto.builder()
                .id(notification.getId())
                .event(notification.getEvent().toString())
                .createdAt(notification.getCreatedAt())
                .isActive(notification.isActive())
                .build())
            .build();
    }

}
