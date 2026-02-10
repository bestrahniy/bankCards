package com.example.bankcards.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.mapper.interfaces.specializedInterface.PageMapper;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper for paginated response transformations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PageMapperImpl implements PageMapper {

    private final NotificationMapperImpl notificationMapper;
    private final BankCardMapperImpl bankCardMapper;

    /**
     * Maps Page<NotificationEntity> to PageResponse<NotificationResponse>.
     * 
     * @param page paginated notification entities
     * @return paginated notification response DTO
     */
    @Override
    public PageResponse<NotificationResponse> toDtoNotification(Page<NotificationEntity> page) {
        log.debug("Mapping Page<NotificationEntity> to PageResponse (page: {}, size: {}, total: {})", 
            page.getNumber(), page.getSize(), page.getTotalElements());
        
        List<NotificationResponse> content = page.getContent().stream()
            .map(notification -> notificationMapper.toDto(notification.getUser(), notification))
            .toList();
        
        log.trace("Mapped {} notification entities", content.size());
        
        return PageResponse.<NotificationResponse>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }

    /**
     * Maps Page<BankCardsEntity> to PageResponse<CardResponse>.
     * 
     * @param page paginated bank card entities
     * @return paginated card response DTO
     */
    @Override
    public PageResponse<CardResponse> toDtoBankCards(Page<BankCardsEntity> page) {
        log.debug("Mapping Page<BankCardsEntity> to PageResponse (page: {}, size: {}, total: {})", 
            page.getNumber(), page.getSize(), page.getTotalElements());
        
        List<CardResponse> content = page.getContent().stream()
            .map(bankCard -> bankCardMapper.toDtoCardResponse(bankCard))
            .toList();
        
        log.trace("Mapped {} bank card entities", content.size());
        
        return PageResponse.<CardResponse>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }

}