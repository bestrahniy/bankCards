package com.example.bankcards.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import com.example.bankcards.crypto.AesEncryption;
import com.example.bankcards.exception.bankCardException.BankCardNotFoundException;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.repository.BankCardsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating access to bank cards.
 * Provides validation logic for card operations
 * Uses AES encryption for secure card number handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardAccessValidator {

    private final BankCardsRepository bankCardsRepository;
    private final AesEncryption aesEncryption;

    /**
     * Finds a bank card by its number with encryption handling.
     * 
     * Encrypts the provided card number before querying the database
     * to match stored encrypted values.
     * 
     * @param cardNumber plaintext card number to find
     * @return bank card entity if found
     * @throws BankCardNotFoundException if card doesn't exist
     */
    public BankCardsEntity findBankCardByNumber(String cardNumber) {
        log.debug("Finding bank card by number: {}", 
            cardNumber != null ? "****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4)) : "null");
        
        String encryptedCardNumber = aesEncryption.encrypt(cardNumber);
        log.trace("Card number encrypted for lookup");
        
        return bankCardsRepository.findByNumber(encryptedCardNumber)
            .orElseThrow(() -> {
                log.error("Bank card not found for number: {}", 
                    cardNumber != null ? "****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4)) : "null");
                return new BankCardNotFoundException(cardNumber);
            });
    }

    /**
     * Validates card for general usage.
     * 
     * Checks both active status and expiration date.
     * Card must be active and not expired to pass validation.
     * 
     * @param currentNumber card number to validate
     * @return true if card is valid for use, false otherwise
     */
    public Boolean checkCard(String currentNumber) {
        log.debug("Validating card: {}", 
            currentNumber != null ? "****" + currentNumber.substring(Math.max(0, currentNumber.length() - 4)) : "null");
        
        BankCardsEntity bankCard = findBankCardByNumber(currentNumber);
        
        boolean isActive = checkActiveCard(bankCard);
        boolean notExpired = checkExpiresCard(bankCard);
        boolean isValid = isActive && notExpired;
        
        log.debug("Card validation result - Active: {}, Not expired: {}, Overall: {}", 
            isActive, notExpired, isValid);
        
        return isValid;
    }

    /**
     * Checks if card is currently active.
     * 
     * @param bankCard card entity to check
     * @return true if card is active, false if blocked/suspended
     */
    public Boolean checkActiveCard(BankCardsEntity bankCard) {
        boolean isActive = bankCard.getIsActive();
        log.trace("Card active status check: {}", isActive);
        return isActive;
    }

    /**
     * Checks if card has not expired.
     * 
     * Compares expiration date with current time.
     * 
     * @param bankCard card entity to check
     * @return true if card expiration date is in future
     */
    public Boolean checkExpiresCard(BankCardsEntity bankCard) {
        boolean notExpired = bankCard.getExpiresAt().isAfter(Instant.now());
        log.trace("Card expiration check - Expires at: {}, Not expired: {}", 
            bankCard.getExpiresAt(), notExpired);
        return notExpired;
    }

}