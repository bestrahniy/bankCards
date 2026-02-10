package com.example.bankcards.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.exception.bankCardException.BankCardNotAvailableException;
import com.example.bankcards.exception.bankCardException.BankCardNotEnoughFundsException;
import com.example.bankcards.exception.requestException.AmountIsSmallException;
import com.example.bankcards.exception.userException.UserNotActiveException;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.mapper.PaymentTransactionsMapperImpl;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.PaymentTransactionsEntity;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.PaymentTransactionsRepository;
import com.example.bankcards.service.interfaces.TransferService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final SecurityFacade securityFacade;

    private final PaymentTransactionsMapperImpl paymentTransactionsMapper;

    private final PaymentTransactionsRepository paymentTransactionsRepository;

    private final CardAccountRepository cardAccountRepository;

    private final ValidationServiceImpl validationService;
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CreateTransactionResponse transfer(TransferRequest transferRequest) throws IllegalAccessException {

        validationService.validateCurrentUserIsActive();

        validationService.validateCardsAvailability(transferRequest);

        validationService.validateAmount(transferRequest.getAmount());
    
        BankCardsEntity fromCard = securityFacade.findBankCardByNumber(transferRequest.getFromNumberCard());
        CardAccountEntity fromAccount = fromCard.getCardAccountEntity();

        BankCardsEntity toCard = securityFacade.findBankCardByNumber(transferRequest.getToNumberCard());
        CardAccountEntity toAccount = toCard.getCardAccountEntity();

        validationService.validateSufficientFunds(fromAccount, transferRequest.getAmount());

        performTransfer(fromAccount, toAccount, transferRequest.getAmount());

        PaymentTransactionsEntity transaction = createTransaction(
            fromAccount,
            toCard.getId(),
            transferRequest
        );

        updateCardTransactions(fromCard, toCard, transaction);

        saveUpdatedAccounts(fromAccount, toAccount);
        
        return paymentTransactionsMapper.toDto(transaction);
    }

    private void performTransfer(CardAccountEntity fromAccount, CardAccountEntity toAccount, Double amount) {
        Double newFromBalance = fromAccount.getCurrentBalance() - amount;
        fromAccount.setCurrentBalance(newFromBalance);

        Double newToBalance = toAccount.getCurrentBalance() + amount;
        toAccount.setCurrentBalance(newToBalance);
    }

    private void updateCardTransactions(BankCardsEntity fromCard, BankCardsEntity toCard,
                                    PaymentTransactionsEntity transaction) {
        if (fromCard.getCardAccountEntity().getPaymentTransactionsEntities() == null) {
            fromCard.getCardAccountEntity().setPaymentTransactionsEntities(new ArrayList<>());
        }
        fromCard.getCardAccountEntity().getPaymentTransactionsEntities().add(transaction);

        if (toCard.getCardAccountEntity().getPaymentTransactionsEntities() == null) {
            toCard.getCardAccountEntity().setPaymentTransactionsEntities(new ArrayList<>());
        }
        toCard.getCardAccountEntity().getPaymentTransactionsEntities().add(transaction);
    }

    private void saveUpdatedAccounts(CardAccountEntity fromAccount, CardAccountEntity toAccount) {
        cardAccountRepository.save(fromAccount);
        cardAccountRepository.save(toAccount);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private PaymentTransactionsEntity createTransaction(CardAccountEntity cardAccountEntity,
        UUID recipientAccountId, TransferRequest transferRequest) {
        PaymentTransactionsEntity paymentTransactionsEntity = paymentTransactionsMapper.toEntity(
            cardAccountEntity, recipientAccountId, transferRequest);
        return paymentTransactionsRepository.save(paymentTransactionsEntity);
    }

}
