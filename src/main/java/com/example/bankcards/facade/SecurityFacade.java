package com.example.bankcards.facade;

import org.springframework.stereotype.Service;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.service.CardAccessValidator;
import com.example.bankcards.service.SecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Security facade providing unified access to security-related functionality.
 * 
 * Acts as a single entry point for security operations, abstracting
 * the complexity of underlying security services. Provides methods
 * for user context retrieval, card validation, and authentication checks.
 * 
 * This facade follows the Facade design pattern to simplify security
 * interactions throughout the application.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityFacade {

    private final SecurityContextService contextService;
    private final CardAccessValidator cardValidator;

    /**
     * Retrieves the currently authenticated user entity.
     * 
     * @return current authenticated user
     * @throws UserNotFoundException if user doesn't exist
     */
    public UsersEntity getCurrentUser() {
        log.trace("Retrieving current user via SecurityContextService");
        return contextService.getCurrentUser();
    }

    /**
     * Validates a payment card for usage.
     * Checks if card exists, is active, and not expired.
     * 
     * @param currentNumber card number to validate
     * @return true if card is valid for transactions
     */
    public Boolean checkCard(String currentNumber) {
        log.debug("Validating card availability: {}", 
            currentNumber != null ? "****" + currentNumber.substring(Math.max(0, currentNumber.length() - 4)) : "null");
        return cardValidator.checkCard(currentNumber);
    }

    /**
     * Checks if current user account is active.
     * 
     * @return true if current user account is active
     */
    public Boolean isCurrentActive() {
        boolean isActive = contextService.isCurrentActive();
        log.trace("Current user active status: {}", isActive);
        return isActive;
    }

    /**
     * Retrieves login of current authenticated user.
     * 
     * @return current user's login
     */
    public String getLogin() {
        String login = contextService.getLogin();
        log.trace("Current user login: {}", login);
        return login;
    }

    /**
     * Finds a bank card by its number.
     * Uses encryption for secure card number lookup.
     * 
     * @param cardNumber card number to find
     * @return bank card entity
     * @throws BankCardNotFoundException if card doesn't exist
     */
    public BankCardsEntity findBankCardByNumber(String cardNumber) {
        log.debug("Finding bank card by number: {}", 
            cardNumber != null ? "****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4)) : "null");
        return cardValidator.findBankCardByNumber(cardNumber);
    }

}
