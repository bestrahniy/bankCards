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

@Service
@RequiredArgsConstructor
public class AdminCardServiceImpl implements AdminCardService {

    private final UsersRepository usersRepository;

    private final CardAccountMapperImpl cardAccountMapper;

    private final CardAccountRepository cardAccountRepository;

    private final BankCardMapperImpl bankCardMapper;

    private final BankCardsRepository bankCardsRepository;

    private final SecurityFacade securityFacade;

    private final ValidationServiceImpl validationService;

    @Transactional
    public CreateCardResponse createCard(UUID userId) {
        UsersEntity user = usersRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        validationService.validateUserIsActive(user);

        CardAccountEntity cardAccount = cardAccountMapper.toEntity();
        cardAccount = cardAccountRepository.save(cardAccount);

        BankCardsEntity bankCard = bankCardMapper.toEntity(cardAccount);
        bankCard.setUser(user);
        bankCard.setCardAccountEntity(cardAccount);

        cardAccount.setBankCardsEntity(bankCard);

        bankCard = bankCardsRepository.save(bankCard);

        if (user.getBankCardsEntities() == null) {
            user.setBankCardsEntities(new ArrayList<>());
        }

        user.getBankCardsEntities().add(bankCard);

        return bankCardMapper.toDtoCreateCardResponse(bankCard);
    }

    @Transactional
    public CardActiveStatusResponse blockCard(CardNumberRequest cardNumberRequest) {
        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(cardNumberRequest.getCardNumber());
        bankCard.setIsActive(false);
        return bankCardMapper.toDtoCardActiveStatusResponse(bankCard);
    }

    @Transactional
    public CardActiveStatusResponse unblockCard(CardNumberRequest cardNumberRequest) {
        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(cardNumberRequest.getCardNumber());
        bankCard.setIsActive(true);
        return bankCardMapper.toDtoCardActiveStatusResponse(bankCard);
    }

}
