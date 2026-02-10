package com.example.bankcards.mapper.interfaces.specializedInterface;

import com.example.bankcards.dto.response.CardActiveStatusResponse;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;

public interface BankCardMapper  {

    BankCardsEntity toEntity(CardAccountEntity cardAccount);

    CreateCardResponse toDtoCreateCardResponse(BankCardsEntity bankCardsEntity);

    CardResponse toDtoCardResponse(BankCardsEntity bankCardsEntity);

    CardStatusResponse toDtoCardStatusResponse(BankCardsEntity bankCardsEntity);

    CardActiveStatusResponse toDtoCardActiveStatusResponse(BankCardsEntity bankCardsEntity);

}
