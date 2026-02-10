package com.example.bankcards.mapper.interfaces.specializedInterface;

import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.UsersEntity;

public interface NotificationMapper {

    NotificationEntity toEntity(UsersEntity user);

    NotificationEntity toEntity(UsersEntity user, CardAccountEntity cardAccount);

    NotificationResponse toDto(UsersEntity user, NotificationEntity notification);

}
