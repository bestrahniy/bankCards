package com.example.bankcards.service.interfaces;

import java.util.UUID;

import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardActiveStatusResponse;
import com.example.bankcards.dto.response.CreateCardResponse;

public interface AdminCardService {

    CreateCardResponse createCard(UUID userId);

    CardActiveStatusResponse blockCard(CardNumberRequest cardNumberRequest);

    CardActiveStatusResponse unblockCard(CardNumberRequest cardNumberRequest);

}
