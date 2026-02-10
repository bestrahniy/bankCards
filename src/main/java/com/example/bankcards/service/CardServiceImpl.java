package com.example.bankcards.service;

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

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final UsersRepository usersRepository;

    private final NotificationMapperImpl notificationMapper;

    private final NotificationRepository notificationRepository;

    private final SecurityFacade securityFacade;

    private final BankCardsRepository bankCardsRepository;

    private final PageMapperImpl pageMapper;

    private final BankCardMapperImpl bankCardMapper;

    private final ValidationServiceImpl validationService;

    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationResponse createCardRequest(UserDetails userDetails) {

        validationService.validateCurrentUserIsActive();

        UsersEntity user = usersRepository.findByLoginWithRoles(userDetails.getUsername())
            .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));

        NotificationEntity notificationEntity = notificationMapper.toEntity(user);
        notificationRepository.save(notificationEntity);

        return notificationMapper.toDto(user, notificationEntity);
    }

    public PageResponse<CardResponse> showAllCards(Integer page, Integer size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        UsersEntity user = securityFacade.getCurrentUser();
    
        Page<BankCardsEntity> pageResult = bankCardsRepository.findActiveCardsByUser(user, pageable);

        return pageMapper.toDtoBankCards(pageResult);
    }

    public CardStatusResponse checkBalance(CardNumberRequest cardNumberRequest) {

        validationService.validateCurrentUserIsActive();

        boolean validateCard = securityFacade.checkCard(cardNumberRequest.getCardNumber());

        if (!validateCard) {
            throw new BankCardNotAvailableException("One or both cards are not available");
        }

        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(cardNumberRequest.getCardNumber());

        return bankCardMapper.toDtoCardStatusResponse(bankCard);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationResponse createBlockRequest(CardNumberRequest cardNumberRequest) {
        validationService.validateCurrentUserIsActive();

        UsersEntity usersEntity = securityFacade.getCurrentUser();
        NotificationEntity notificationEntity = notificationMapper.toEntity(usersEntity,
            securityFacade.findBankCardByNumber(cardNumberRequest.getCardNumber()).getCardAccountEntity());

        notificationRepository.save(notificationEntity);
        setNotificationToCardAccount(notificationEntity, cardNumberRequest.getCardNumber());

        return notificationMapper.toDto(usersEntity, notificationEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void setNotificationToCardAccount(NotificationEntity notificationEntity, String cardNumber) {
        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(cardNumber);
        CardAccountEntity cardAccount = bankCard.getCardAccountEntity();

        List<NotificationEntity> notifications = cardAccount.getNotificationEntities();
        notifications.add(notificationEntity);

        cardAccount.setNotificationEntities(notifications);
    }

}
