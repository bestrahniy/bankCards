package com.example.bankcards.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.mapper.PaymentTransactionsMapperImpl;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.PaymentTransactionsEntity;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.PaymentTransactionsRepository;
import com.example.bankcards.service.interfaces.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for processing money transfers between payment cards.
 * Implements core business logic for transferring funds
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final SecurityFacade securityFacade;

    private final PaymentTransactionsMapperImpl paymentTransactionsMapper;

    private final PaymentTransactionsRepository paymentTransactionsRepository;

    private final CardAccountRepository cardAccountRepository;

    private final ValidationServiceImpl validationService;

    /**
     * Processes a money transfer between two payment cards.
     * 
     * Performs comprehensive validation including user activity,
     * card availability, sufficient funds, and amount validation.
     * Updates balances and creates audit transaction record.
     * 
     * @param transferRequest transfer details including cards and amount
     * @return response with transaction details
     * @throws IllegalAccessException if security validation fails
     * @throws BankCardNotAvailableException if cards are unavailable
     * @throws BankCardNotEnoughFundsException if insufficient balance
     * @throws UserNotActiveException if user account is suspended
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CreateTransactionResponse transfer(TransferRequest transferRequest) throws IllegalAccessException {
        log.info("Processing transfer from {} to {} for amount: {}", 
            transferRequest.getFromNumberCard() != null ? 
                "****" + transferRequest.getFromNumberCard().substring(Math.max(0, transferRequest.getFromNumberCard().length() - 4)) : "null",
            transferRequest.getToNumberCard() != null ? 
                "****" + transferRequest.getToNumberCard().substring(Math.max(0, transferRequest.getToNumberCard().length() - 4)) : "null",
            transferRequest.getAmount());
        
        validationService.validateCurrentUserIsActive();
        log.debug("User activity validation passed");

        validationService.validateCardsAvailability(transferRequest);
        log.debug("Card availability validation passed");

        validationService.validateAmount(transferRequest.getAmount());
        log.debug("Amount validation passed: {}", transferRequest.getAmount());
    
        BankCardsEntity fromCard = securityFacade.findBankCardByNumber(transferRequest.getFromNumberCard());
        CardAccountEntity fromAccount = fromCard.getCardAccountEntity();
        log.debug("Sender card found, account ID: {}, balance: {}", 
            fromAccount.getId(), fromAccount.getCurrentBalance());

        BankCardsEntity toCard = securityFacade.findBankCardByNumber(transferRequest.getToNumberCard());
        CardAccountEntity toAccount = toCard.getCardAccountEntity();
        log.debug("Recipient card found, account ID: {}, balance: {}", 
            toAccount.getId(), toAccount.getCurrentBalance());

        validationService.validateSufficientFunds(fromAccount, transferRequest.getAmount());
        log.debug("Sufficient funds validation passed");

        performTransfer(fromAccount, toAccount, transferRequest.getAmount());
        log.info("Balances updated - From: {} -> {}, To: {} -> {}", 
            fromAccount.getCurrentBalance() + transferRequest.getAmount(), fromAccount.getCurrentBalance(),
            toAccount.getCurrentBalance() - transferRequest.getAmount(), toAccount.getCurrentBalance());

        PaymentTransactionsEntity transaction = createTransaction(
            fromAccount,
            toCard.getId(),
            transferRequest
        );
        log.info("Transaction recorded with ID: {}", transaction.getId());

        updateCardTransactions(fromCard, toCard, transaction);
        log.debug("Transaction linked to card accounts");

        saveUpdatedAccounts(fromAccount, toAccount);
        log.debug("Account updates persisted");
        
        log.info("Transfer completed successfully. Transaction ID: {}", transaction.getId());
        return paymentTransactionsMapper.toDto(transaction);
    }

    /**
     * Updates balances between sender and recipient accounts.
     * 
     * @param fromAccount sender's card account
     * @param toAccount recipient's card account
     * @param amount transfer amount
     */
    private void performTransfer(CardAccountEntity fromAccount, CardAccountEntity toAccount, Double amount) {
        Double newFromBalance = fromAccount.getCurrentBalance() - amount;
        fromAccount.setCurrentBalance(newFromBalance);

        Double newToBalance = toAccount.getCurrentBalance() + amount;
        toAccount.setCurrentBalance(newToBalance);
        
        log.trace("Transfer performed: -{} from account {}, +{} to account {}", 
            amount, fromAccount.getId(), amount, toAccount.getId());
    }

    /**
     * Links transaction to both sender and recipient card accounts.
     * 
     * @param fromCard sender's bank card
     * @param toCard recipient's bank card
     * @param transaction created transaction entity
     */
    private void updateCardTransactions(BankCardsEntity fromCard, BankCardsEntity toCard,
                                    PaymentTransactionsEntity transaction) {
        if (fromCard.getCardAccountEntity().getPaymentTransactionsEntities() == null) {
            fromCard.getCardAccountEntity().setPaymentTransactionsEntities(new ArrayList<>());
            log.trace("Initialized transactions list for sender account");
        }
        fromCard.getCardAccountEntity().getPaymentTransactionsEntities().add(transaction);

        if (toCard.getCardAccountEntity().getPaymentTransactionsEntities() == null) {
            toCard.getCardAccountEntity().setPaymentTransactionsEntities(new ArrayList<>());
            log.trace("Initialized transactions list for recipient account");
        }
        toCard.getCardAccountEntity().getPaymentTransactionsEntities().add(transaction);
        
        log.trace("Transaction added to both card accounts");
    }

    /**
     * Persists updated account balances to database.
     * 
     * @param fromAccount sender's updated account
     * @param toAccount recipient's updated account
     */
    private void saveUpdatedAccounts(CardAccountEntity fromAccount, CardAccountEntity toAccount) {
        cardAccountRepository.save(fromAccount);
        cardAccountRepository.save(toAccount);
        log.trace("Account balances persisted");
    }

    /**
     * Creates and saves transaction record for audit trail.
     * 
     * @param cardAccountEntity sender's account
     * @param recipientAccountId recipient's account ID
     * @param transferRequest original transfer request
     * @return saved transaction entity
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private PaymentTransactionsEntity createTransaction(CardAccountEntity cardAccountEntity,
        UUID recipientAccountId, TransferRequest transferRequest) {
        log.debug("Creating transaction record for transfer");
        
        PaymentTransactionsEntity paymentTransactionsEntity = paymentTransactionsMapper.toEntity(
            cardAccountEntity, recipientAccountId, transferRequest);
        
        PaymentTransactionsEntity savedEntity = paymentTransactionsRepository.save(paymentTransactionsEntity);
        log.trace("Transaction saved with ID: {}", savedEntity.getId());
        
        return savedEntity;
    }

}