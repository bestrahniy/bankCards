package com.example.bankcards.facade;

import org.springframework.stereotype.Service;

import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.service.CardAccessValidator;
import com.example.bankcards.service.SecurityContextService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityFacade {

    private final SecurityContextService contextService;

    private final CardAccessValidator cardValidator;

    public UsersEntity getCurrentUser() {
        return contextService.getCurrentUser();
    }

    public Boolean checkCard(String currentNumber) {
        return cardValidator.checkCard(currentNumber);
    }

    public Boolean isCurrentActive() {
        return contextService.isCurrentActive();
    }

    public String getLogin() {
        return contextService.getLogin();
    }

    public BankCardsEntity findBankCardByNumber(String cardNumber) {
        return cardValidator.findBankCardByNumber(cardNumber);
    }

}
