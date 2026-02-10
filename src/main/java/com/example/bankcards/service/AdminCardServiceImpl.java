package com.example.bankcards.service;

import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardActiveStatusResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.exception.userException.UserNotFoundException;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.mapper.BankCardMapperImpl;
import com.example.bankcards.mapper.CardAccountMapperImpl;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.interfaces.AdminCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Administrative card management service implementation.
 * 
 * Provides administrative operations for card management including
 * card creation, blocking, and unblocking. All operations require
 * administrator privileges which are enforced at controller level.
 * 
 * Transactions are managed with @Transactional to ensure data consistency
 * during card lifecycle operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCardServiceImpl implements AdminCardService {

    private final UsersRepository usersRepository;

    private final CardAccountMapperImpl cardAccountMapper;

    private final CardAccountRepository cardAccountRepository;

    private final BankCardMapperImpl bankCardMapper;

    private final BankCardsRepository bankCardsRepository;

    private final SecurityFacade securityFacade;

    private final ValidationServiceImpl validationService;

    /**
     * Creates a new bank card for the specified user.
     * 
     * Performs validation to ensure user exists and is active,
     * then creates associated card account and bank card entities.
     * 
     * @param userId unique identifier of the user receiving the card
     * @return response with created card details
     * @throws UserNotFoundException if user with given ID doesn't exist
     * @throws UserNotActiveException if user account is suspended
     */
    @Transactional
    public CreateCardResponse createCard(UUID userId) {
        log.info("Starting card creation for user ID: {}", userId);
        
        UsersEntity user = usersRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found for card creation: {}", userId);
                return new UserNotFoundException(userId);
            });

        log.debug("Validating user activity status for: {}", user.getLogin());
        validationService.validateUserIsActive(user);
        log.debug("User validation passed for: {}", user.getLogin());

        log.trace("Creating card account entity...");
        CardAccountEntity cardAccount = cardAccountMapper.toEntity();
        cardAccount = cardAccountRepository.save(cardAccount);
        log.debug("Card account created with ID: {}", cardAccount.getId());

        log.trace("Creating bank card entity...");
        BankCardsEntity bankCard = bankCardMapper.toEntity(cardAccount);
        bankCard.setUser(user);
        bankCard.setCardAccountEntity(cardAccount);
        cardAccount.setBankCardsEntity(bankCard);

        bankCard = bankCardsRepository.save(bankCard);
        log.info("Bank card created successfully with number: {}", 
            bankCard.getNumber() != null ? "ENCRYPTED" : "PENDING");

        if (user.getBankCardsEntities() == null) {
            log.trace("Initializing user cards collection");
            user.setBankCardsEntities(new ArrayList<>());
        }

        user.getBankCardsEntities().add(bankCard);
        log.debug("Card added to user's collection. Total cards: {}", 
            user.getBankCardsEntities().size());

        return bankCardMapper.toDtoCreateCardResponse(bankCard);
    }

    /**
     * Blocks an existing bank card preventing further transactions.
     * 
     * Sets the card's active status to false. Card can be
     * unblocked later using unblockCard method.
     * 
     * @param cardNumberRequest contains card number to block
     * @return response with updated card status
     * @throws BankCardNotFoundException if card doesn't exist
     */
    @Transactional
    public CardActiveStatusResponse blockCard(CardNumberRequest cardNumberRequest) {
        log.info("Blocking card: {}", cardNumberRequest.getCardNumber());
        
        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(
            cardNumberRequest.getCardNumber());
        log.debug("Card found, current active status: {}", bankCard.getIsActive());
        
        bankCard.setIsActive(false);
        log.info("Card blocked successfully: {}", cardNumberRequest.getCardNumber());
        
        return bankCardMapper.toDtoCardActiveStatusResponse(bankCard);
    }

    /**
     * Unblocks a previously blocked bank card.
     * 
     * Restores card's active status to true, allowing
     * transactions to resume.
     * 
     * @param cardNumberRequest contains card number to unblock
     * @return response with updated card status
     * @throws BankCardNotFoundException if card doesn't exist
     */
    @Transactional
    public CardActiveStatusResponse unblockCard(CardNumberRequest cardNumberRequest) {
        log.info("Unblocking card: {}", cardNumberRequest.getCardNumber());
        
        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(
            cardNumberRequest.getCardNumber());
        log.debug("Card found, current active status: {}", bankCard.getIsActive());
        
        bankCard.setIsActive(true);
        log.info("Card unblocked successfully: {}", cardNumberRequest.getCardNumber());
        
        return bankCardMapper.toDtoCardActiveStatusResponse(bankCard);
    }

}