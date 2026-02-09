package com.example.bankcards.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.EntityProjection.ProjectionType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.AmountIsSmallException;
import com.example.bankcards.exception.BankCardNotAvailableException;
import com.example.bankcards.exception.BankCardNotEnoughFundsException;
import com.example.bankcards.exception.BankCardNotFoundException;
import com.example.bankcards.exception.UserNotActiveException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.mapper.BankCardMapper;
import com.example.bankcards.mapper.NotificationMapper;
import com.example.bankcards.mapper.PageMapper;
import com.example.bankcards.mapper.PaymentTransactionsMapper;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.PaymentTransactionsEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.NotificationRepository;
import com.example.bankcards.repository.PaymentTransactionsRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.util.AesEncryption;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtCreator jwtCreator;

    private final RefreshTokenService refreshTokenService;

    private final NotificationMapper notificationMapper;

    private final NotificationRepository notificationRepository;

    private final BankCardsRepository bankCardsRepository;

    private final PageMapper pageMapper;

    private final AesEncryption aesEncryption;

    private final SecurityFacade securityFacade;

    private final PaymentTransactionsRepository paymentTransactionsRepository;

    private final PaymentTransactionsMapper paymentTransactionsMapper;

    private final CardAccountRepository cardAccountRepository;

    private final BankCardMapper bankCardMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserResponse registerUser(RegistrationRequest registrationRequest) {
        UsersEntity user = userMapper.toEntity(registrationRequest);

        user.setPassword(
            passwordEncoder.encode(
                registrationRequest.getPassword()
        ));

        user.setRoles(
            roleRepository.findByRole(RoleType.USER)
                .stream()
                    .collect(Collectors.toSet())
        );

        usersRepository.save(user);

        return userMapper.toDto(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserResponse authorizationUser(AuthorizationRequest authorizationRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authorizationRequest.getLogin(),
                authorizationRequest.getPassword()
            )
        );

        UsersEntity user = usersRepository.findByLogin(authorizationRequest.getLogin())
            .orElseThrow(() -> new UserNotFoundException(authorizationRequest.getLogin()));

        String jwtToken = jwtCreator.createJwt(user);
        refreshTokenService.createRefershToken(user);

        return userMapper.toDto(user, jwtToken);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationResponse createCardRequest(UserDetails userDetails) {

        validateCurrentUserIsActive();

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

    @Transactional(propagation = Propagation.REQUIRED)
    public CreateTransactionResponse transfer(TransferRequest transferRequest) throws IllegalAccessException {

        validateCurrentUserIsActive();

        validateCardsAvailability(transferRequest);

        validateAmount(transferRequest.getAmount());
    
        BankCardsEntity fromCard = securityFacade.findBankCardByNumber(transferRequest.getFromNumberCard());
        CardAccountEntity fromAccount = fromCard.getCardAccountEntity();

        BankCardsEntity toCard = securityFacade.findBankCardByNumber(transferRequest.getToNumberCard());
        CardAccountEntity toAccount = toCard.getCardAccountEntity();

        validateSufficientFunds(fromAccount, transferRequest.getAmount());

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

    public CardStatusResponse checkBalance(CardNumberRequest cardNumberRequest) throws IllegalAccessException {

        validateCurrentUserIsActive();

        boolean validateCard = securityFacade.checkCard(cardNumberRequest.getCardNumber());

        if (!validateCard) {
            throw new BankCardNotAvailableException("One or both cards are not available");
        }

        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(cardNumberRequest.getCardNumber());

        return bankCardMapper.toDtoCardStatusResponse(bankCard);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationResponse createBlockRequest(CardNumberRequest cardNumberRequest) {
        validateCurrentUserIsActive();

        UsersEntity usersEntity = securityFacade.getCurrentUser();
        NotificationEntity notificationEntity = notificationMapper.toEntity(usersEntity,
            securityFacade.findBankCardByNumber(cardNumberRequest.getCardNumber()).getCardAccountEntity());

        notificationRepository.save(notificationEntity);
        setNotificationToCardAccount(notificationEntity, cardNumberRequest.getCardNumber());

        return notificationMapper.toDto(usersEntity, notificationEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void setNotificationToCardAccount(NotificationEntity notificationEntity, String cardNumber) {
        BankCardsEntity bankCard = securityFacade.findBankCardByNumber(cardNumber);
        CardAccountEntity cardAccount = bankCard.getCardAccountEntity();

        List<NotificationEntity> notifications = cardAccount.getNotificationEntities();
        notifications.add(notificationEntity);

        cardAccount.setNotificationEntities(notifications);
    }

    private void validateCurrentUserIsActive() {
        if (!securityFacade.isCurrentActive()) {
            throw new UserNotActiveException(securityFacade.getLogin());
        }
    }

    private void validateAmount(Double amount) {
        if (amount < 1.00) {
            throw new AmountIsSmallException(String.format("amount: %s is small", amount));
        }
    }

    private void validateCardsAvailability(TransferRequest transferRequest) throws IllegalAccessException {
        String fromCardNumber = transferRequest.getFromNumberCard();
        String toCardNumber = transferRequest.getToNumberCard();
        
        boolean fromCardAvailable = securityFacade.checkCard(fromCardNumber);
        boolean toCardAvailable = securityFacade.checkCard(toCardNumber);
        
        if (!fromCardAvailable || !toCardAvailable) {
            throw new BankCardNotAvailableException("One or both cards are not available");
        }
    }

    private void validateSufficientFunds(CardAccountEntity fromAccount, Double amount) {
        if (fromAccount.getCurrentBalance() - amount < 0) {
            throw new BankCardNotEnoughFundsException();
        }
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
