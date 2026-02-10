package com.example.bankcards.service.interfaces;

import org.springframework.security.core.userdetails.UserDetails;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.model.entity.NotificationEntity;

public interface CardService {

    NotificationResponse createCardRequest(UserDetails userDetails);

    PageResponse<CardResponse> showAllCards(Integer page, Integer size);

    CardStatusResponse checkBalance(CardNumberRequest cardNumberRequest);

    NotificationResponse createBlockRequest(CardNumberRequest cardNumberRequest);

    void setNotificationToCardAccount(NotificationEntity notificationEntity, String cardNumber);

}
