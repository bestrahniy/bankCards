package com.example.bankcards.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.exception.bankCardException.BankCardNotAvailableException;
import com.example.bankcards.exception.userException.UserNotFoundException;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.mapper.BankCardMapperImpl;
import com.example.bankcards.mapper.NotificationMapperImpl;
import com.example.bankcards.mapper.PageMapperImpl;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.NotificationRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.interfaces.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for user card management operations.
 * Provides functionality for regular users to manage their cards
 * Includes pagination support and comprehensive validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final UsersRepository usersRepository;

    private final NotificationMapperImpl notificationMapper;

    private final NotificationRepository notificationRepository;

    private final SecurityFacade securityFacade;

    private final BankCardsRepository bankCardsRepository;

    private final PageMapperImpl pageMapper;

    private final BankCardMapperImpl bankCardMapper;

    private final ValidationServiceImpl validationService;

    /**
     * Creates a new craete card request for the admin.
     * 
     * Validates user is active, then creates a notification
     * for administrators to process the card creation request.
     * 
     * @param userDetails authenticated user details
     * @return notification response with request details
     * @throws UserNotActiveException if user account is suspended
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationResponse createCardRequest(UserDetails userDetails) {
        log.info("Creating card request for user: {}", userDetails.getUsername());
        
        validationService.validateCurrentUserIsActive();
        log.debug("User activity validation passed");

        UsersEntity user = usersRepository.findByLoginWithRoles(userDetails.getUsername())
            .orElseThrow(() -> {
                log.error("User not found for card request: {}", userDetails.getUsername());
                return new UserNotFoundException(userDetails.getUsername());
            });
        log.debug("User loaded with ID: {}", user.getId());

        NotificationEntity notificationEntity = notificationMapper.toEntity(user);
        notificationRepository.save(notificationEntity);
        log.info("Card request notification created with ID: {}", notificationEntity.getId());

        return notificationMapper.toDto(user, notificationEntity);
    }

    /**
     * Retrieves paginated list of user's active cards.
     * 
     * Returns cards sorted by creation date.
     * Includes pagination metadata for client-side navigation.
     * 
     * @param page zero-based page index
     * @param size number of items per page
     * @return paginated response with card details
     */
    public PageResponse<CardResponse> showAllCards(Integer page, Integer size) {
        log.debug("Fetching cards page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        log.trace("Pageable created with sort: createdAt DESC");

        UsersEntity user = securityFacade.getCurrentUser();
        log.debug("Fetching cards for user: {}", user.getLogin());

        Page<BankCardsEntity> pageResult = bankCardsRepository.findActiveCardsByUser(user, pageable);
        log.debug("Found {} cards on page {} of {}", 
            pageResult.getNumberOfElements(), page, pageResult.getTotalPages());

        return pageMapper.toDtoBankCards(pageResult);
    }

    /**
     * Checks balance and transactions for a specific card.
     * 
     * Validates card exists, belongs to user, and is active,
     * then returns current balance with recent transaction history.
     * 
     * @param cardNumberRequest contains card number to check
     * @return card status with balance and transactions
     * @throws BankCardNotAvailableException if card validation fails
     * @throws UserNotActiveException if user account is suspended
     */
    public CardStatusResponse checkBalance(CardNumberRequest cardNumberRequest) {
        log.info("Checking balance for card: {}", 
            cardNumberRequest.getCardNumber() != null ? 
            "****" + cardNumberRequest.getCardNumber().substring(Math.max(0, cardNumberRequest.getCardNumber().length() - 4)) : "null");
        
        validationService.validateCurrentUserIsActive();
        log.debug("User activity validation passed");

        boolean validateCard = securityFacade.checkCard(cardNumberRequest.getCardNumber());
        log.debug("Card validation result: {}", validateCard);

        if (!validateCard) {
            log.error("Card validation failed: {}", cardNumberRequest.getCardNumber());
            throw new BankCardNotAvailableException("One or both cards are not available");
        }

        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(cardNumberRequest.getCardNumber());
        log.debug("Card found with account ID: {}", 
            bankCard.getCardAccountEntity() != null ? bankCard.getCardAccountEntity().getId() : "null");

        return bankCardMapper.toDtoCardStatusResponse(bankCard);
    }

    /**
     * Creates a block request for a specific card.
     * 
     * Creates notification for administrators to process
     * card blocking request. Card remains active until approved.
     * 
     * @param cardNumberRequest contains card number to block
     * @return notification response with request details
     * @throws UserNotActiveException if user account is suspended
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationResponse createBlockRequest(CardNumberRequest cardNumberRequest) {
        log.info("Creating block request for card: {}", 
            cardNumberRequest.getCardNumber() != null ? 
            "****" + cardNumberRequest.getCardNumber().substring(Math.max(0, cardNumberRequest.getCardNumber().length() - 4)) : "null");
        
        validationService.validateCurrentUserIsActive();
        log.debug("User activity validation passed");

        UsersEntity usersEntity = securityFacade.getCurrentUser();
        log.debug("Current user: {}", usersEntity.getLogin());

        NotificationEntity notificationEntity = notificationMapper.toEntity(usersEntity,
            securityFacade.findBankCardByNumber(cardNumberRequest.getCardNumber()).getCardAccountEntity());
        
        notificationRepository.save(notificationEntity);
        log.info("Block request notification created with ID: {}", notificationEntity.getId());
        
        setNotificationToCardAccount(notificationEntity, cardNumberRequest.getCardNumber());

        return notificationMapper.toDto(usersEntity, notificationEntity);
    }

    /**
     * Associates a notification with a card account.
     * 
     * Internal method to link notification to card account
     * for tracking and reference purposes.
     * 
     * @param notificationEntity notification to associate
     * @param cardNumber target card number
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void setNotificationToCardAccount(NotificationEntity notificationEntity, String cardNumber) {
        log.debug("Associating notification {} with card: {}", 
            notificationEntity.getId(),
            cardNumber != null ? "****" + cardNumber.substring(Math.max(0, cardNumber.length() - 4)) : "null");
        
        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(cardNumber);
        CardAccountEntity cardAccount = bankCard.getCardAccountEntity();

        List<NotificationEntity> notifications = cardAccount.getNotificationEntities();
        if (notifications == null) {
            notifications = new ArrayList<>();
            log.trace("Initialized notifications list for card account");
        }
        
        notifications.add(notificationEntity);
        log.trace("Notification added to card account. Total notifications: {}", notifications.size());

        cardAccount.setNotificationEntities(notifications);
        log.debug("Notification association completed");
    }

}