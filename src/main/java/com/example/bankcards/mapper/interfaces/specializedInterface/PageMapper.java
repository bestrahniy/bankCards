package com.example.bankcards.mapper.interfaces.specializedInterface;

import org.springframework.data.domain.Page;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.NotificationEntity;

public interface PageMapper {

    PageResponse<NotificationResponse> toDtoNotification(Page<NotificationEntity> page);

    PageResponse<CardResponse> toDtoBankCards(Page<BankCardsEntity> page);

}