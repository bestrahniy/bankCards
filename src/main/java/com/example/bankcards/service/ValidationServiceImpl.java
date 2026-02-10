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
import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating business rules and constraints.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl {

    private final SecurityFacade securityFacade;

    /**
     * Validates that the current authenticated user is active.
     * 
     * @throws UserNotActiveException if current user account is suspended
     */
    public void validateCurrentUserIsActive() {
        String login = securityFacade.getLogin();
        log.debug("Validating current user activity: {}", login);
        
        if (!securityFacade.isCurrentActive()) {
            log.error("Current user is not active: {}", login);
            throw new UserNotActiveException(login);
        }
        log.trace("Current user validation passed");
    }

    /**
     * Validates that a specific user entity is active.
     * 
     * @param user user entity to validate
     * @throws UserNotActiveException if user account is suspended
     */
    public void validateUserIsActive(UsersEntity user) {
        log.debug("Validating user activity: {}", user.getLogin());
        
        if (!user.isActive()) {
            log.error("User is not active: {}", user.getLogin());
            throw new UserNotActiveException(user.getLogin());
        }
        log.trace("User activity validation passed");
    }

    /**
     * Validates that a transfer amount meets minimum requirements.
     * 
     * @param amount transfer amount to validate
     * @throws AmountIsSmallException if amount is less than minimum
     */
    public void validateAmount(Double amount) {
        log.debug("Validating transfer amount: {}", amount);
        
        if (amount < 1.00) {
            log.error("Transfer amount too small: {}", amount);
            throw new AmountIsSmallException(String.format("amount: %s is small", amount));
        }
        log.trace("Amount validation passed: {}", amount);
    }

    /**
     * Validates that both cards in a transfer are available.
     * 
     * Checks that sender and recipient cards exist, are active,
     * and not expired.
     * 
     * @param transferRequest transfer request containing card numbers
     * @throws IllegalAccessException if security check fails
     * @throws BankCardNotAvailableException if cards are unavailable
     */
    public void validateCardsAvailability(TransferRequest transferRequest) throws IllegalAccessException {
        String fromCardNumber = transferRequest.getFromNumberCard();
        String toCardNumber = transferRequest.getToNumberCard();
        
        log.debug("Validating card availability - From: ****{}, To: ****{}",
            fromCardNumber != null && fromCardNumber.length() > 4 ? fromCardNumber.substring(fromCardNumber.length() - 4) : "null",
            toCardNumber != null && toCardNumber.length() > 4 ? toCardNumber.substring(toCardNumber.length() - 4) : "null");
        
        boolean fromCardAvailable = securityFacade.checkCard(fromCardNumber);
        boolean toCardAvailable = securityFacade.checkCard(toCardNumber);
        
        log.trace("Card availability - From: {}, To: {}", fromCardAvailable, toCardAvailable);
        
        if (!fromCardAvailable || !toCardAvailable) {
            log.error("Card validation failed - From available: {}, To available: {}", 
                fromCardAvailable, toCardAvailable);
            throw new BankCardNotAvailableException("One or both cards are not available");
        }
        log.trace("Card availability validation passed");
    }

    /**
     * Validates that sender has sufficient funds for transfer.
     * 
     * @param fromAccount sender's card account
     * @param amount transfer amount
     * @throws BankCardNotEnoughFundsException if insufficient balance
     */
    public void validateSufficientFunds(CardAccountEntity fromAccount, Double amount) {
        log.debug("Validating sufficient funds - Account: {}, Balance: {}, Amount: {}", 
            fromAccount.getId(), fromAccount.getCurrentBalance(), amount);
        
        if (fromAccount.getCurrentBalance() - amount < 0) {
            log.error("Insufficient funds - Account: {}, Balance: {}, Amount: {}", 
                fromAccount.getId(), fromAccount.getCurrentBalance(), amount);
            throw new BankCardNotEnoughFundsException();
        }
        log.trace("Sufficient funds validation passed");
    }

}
