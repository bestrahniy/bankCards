package com.example.bankcards.service;

import org.springframework.stereotype.Service;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.exception.bankCardException.BankCardNotAvailableException;
import com.example.bankcards.exception.bankCardException.BankCardNotEnoughFundsException;
import com.example.bankcards.exception.requestException.AmountIsSmallException;
import com.example.bankcards.exception.userException.UserNotActiveException;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.UsersEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl {

    private final SecurityFacade securityFacade;

    public void validateCurrentUserIsActive() {
        if (!securityFacade.isCurrentActive()) {
            throw new UserNotActiveException(securityFacade.getLogin());
        }
    }

    public void validateUserIsActive(UsersEntity user) {
        if (!user.isActive()) {
            throw new UserNotActiveException(user.getLogin());
        }
    }

    public void validateAmount(Double amount) {
        if (amount < 1.00) {
            throw new AmountIsSmallException(String.format("amount: %s is small", amount));
        }
    }

    public void validateCardsAvailability(TransferRequest transferRequest) throws IllegalAccessException {
        String fromCardNumber = transferRequest.getFromNumberCard();
        String toCardNumber = transferRequest.getToNumberCard();
        
        boolean fromCardAvailable = securityFacade.checkCard(fromCardNumber);
        boolean toCardAvailable = securityFacade.checkCard(toCardNumber);
        
        if (!fromCardAvailable || !toCardAvailable) {
            throw new BankCardNotAvailableException("One or both cards are not available");
        }
    }

    public void validateSufficientFunds(CardAccountEntity fromAccount, Double amount) {
        if (fromAccount.getCurrentBalance() - amount < 0) {
            throw new BankCardNotEnoughFundsException();
        }
    }

}
