package com.example.bankcards.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.mapper.interfaces.specializedInterface.PageMapper;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PageMapperImpl implements PageMapper {

    private final NotificationMapperImpl notificationMapper;

    private final BankCardMapperImpl bankCardMapper;

    @Override
    public PageResponse<NotificationResponse> toDtoNotification(Page<NotificationEntity> page) {
        return PageResponse.<NotificationResponse>builder()
            .content(page.getContent().stream()
                .map(notification -> notificationMapper.toDto(notification.getUser(), notification))
                .toList()
            )
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();

    }

    @Override
    public PageResponse<CardResponse> toDtoBankCards(Page<BankCardsEntity> page) {
        return PageResponse.<CardResponse>builder()
            .content(page.getContent().stream()
                .map(bankCard -> bankCardMapper.toDtoCardResponse(bankCard))
                .toList()
            )
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }

}
