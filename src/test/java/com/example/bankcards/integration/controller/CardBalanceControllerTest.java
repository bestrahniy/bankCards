package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.crypto.AesEncryption;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.PaymentTransactionsEntity;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.TransactionTypeEntity;
import com.example.bankcards.model.entity.TransactionsStatusEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.model.enums.TransactionsStatusType;
import com.example.bankcards.model.enums.TransactionsType;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.PaymentTransactionsRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.TransactionTypeRepository;
import com.example.bankcards.repository.TransactionsStatusRepository;
import com.example.bankcards.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CardBalanceControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17")
    )
            .withDatabaseName("balance_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BankCardsRepository bankCardsRepository;

    @Autowired
    private CardAccountRepository cardAccountRepository;

    @Autowired
    private PaymentTransactionsRepository paymentTransactionsRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Autowired
    private TransactionsStatusRepository transactionsStatusRepository;

    @Autowired
    private AesEncryption aesEncryption;

    @Autowired
    private JwtCreatorConfigTest jwtCreatorConfig;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SecurityFacade securityFacade;

    private UsersEntity testUser;
    private RoleEntity adminRole;
    private RoleEntity userRole;
    private String userToken;
    private BankCardsEntity testCard;
    private String testCardNumber = "1234567812345678";
    private Double testCardBalance = 1500.0;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        paymentTransactionsRepository.deleteAll();
        bankCardsRepository.deleteAll();
        cardAccountRepository.deleteAll();
        usersRepository.deleteAll();

        userRole = roleRepository.findByRole(RoleType.USER)
                .stream()
                .findFirst()
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .role(RoleType.USER)
                                .isActive(true)
                                .build()));

        adminRole = roleRepository.findByRole(RoleType.ADMIN)
                .stream()
                .findFirst()
                .orElseGet(() -> roleRepository.save(
                        RoleEntity.builder()
                                .role(RoleType.ADMIN)
                                .isActive(true)
                                .build()));

        testUser = UsersEntity.builder()
                .login("balanceUser")
                .email("balance@example.com")
                .password(passwordEncoder.encode("pass123"))
                .createdAt(Instant.now())
                .isActive(true)
                .build();

        testUser.setRoles(new HashSet<>());
        testUser.getRoles().add(adminRole);
        testUser.getRoles().add(userRole);
        testUser = usersRepository.save(testUser);

        userToken = jwtCreatorConfig.createToken(testUser);

        testCard = createCardForUser(testUser, testCardNumber, testCardBalance);

        setupTransactionTypesAndStatuses();

        createTestTransactions(testCard);

        when(securityFacade.checkCard(testCardNumber)).thenReturn(true);
        when(securityFacade.findBankCardByNumber(testCardNumber)).thenReturn(testCard);
        when(securityFacade.isCurrentActive()).thenReturn(true);
        when(securityFacade.getCurrentUser()).thenReturn(testUser);
        when(securityFacade.getLogin()).thenReturn(testUser.getLogin());

        when(securityFacade.checkCard(anyString())).thenAnswer(invocation -> {
            String cardNumber = invocation.getArgument(0);
            return cardNumber.equals(testCardNumber);
        });

        when(securityFacade.findBankCardByNumber(anyString())).thenAnswer(invocation -> {
            String cardNumber = invocation.getArgument(0);
            if (cardNumber.equals(testCardNumber)) {
                return testCard;
            }
            return null;
        });
    }

    private BankCardsEntity createCardForUser(UsersEntity user, String cardNumber, Double balance) {
        CardAccountEntity cardAccount = CardAccountEntity.builder()
                .currentBalance(balance)
                .updatedAt(Instant.now())
                .paymentTransactionsEntities(new java.util.ArrayList<>())
                .notificationEntities(new java.util.ArrayList<>())
                .build();

        cardAccount = cardAccountRepository.save(cardAccount);

        String encryptedCardNumber = aesEncryption.encrypt(cardNumber);

        BankCardsEntity card = BankCardsEntity.builder()
                .number(encryptedCardNumber)
                .cvc2(123)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(365 * 5, ChronoUnit.DAYS))
                .isActive(true)
                .user(user)
                .cardAccountEntity(cardAccount)
                .build();

        cardAccount.setBankCardsEntity(card);
        return bankCardsRepository.save(card);
    }

    private void createTestTransactions(BankCardsEntity card) {
        if (card == null || card.getCardAccountEntity() == null) {
            return;
        }

        CardAccountEntity account = card.getCardAccountEntity();

        TransactionTypeEntity transferType = transactionTypeRepository.findByTransactionsType(
                        TransactionsType.TRANSFER)
                .orElseGet(() -> transactionTypeRepository.save(TransactionTypeEntity.builder()
                        .transactionsType(TransactionsType.TRANSFER)
                        .build()));

        TransactionsStatusEntity completedStatus = transactionsStatusRepository.findByTransactionsStatus(
                        TransactionsStatusType.COMPLETED)
                .orElseGet(() -> transactionsStatusRepository.save(TransactionsStatusEntity.builder()
                        .transactionsStatus(TransactionsStatusType.COMPLETED)
                        .build()));

        PaymentTransactionsEntity incomingTx1 = PaymentTransactionsEntity.builder()
                .amount(500.0)
                .comment("Salary")
                .senderCardAccountId(createDummyCardAccount())
                .recipientAccountId(card.getId())
                .transactionType(transferType)
                .transactionsStatus(completedStatus)
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .build();
        incomingTx1 = paymentTransactionsRepository.save(incomingTx1);
        account.getPaymentTransactionsEntities().add(incomingTx1);

        PaymentTransactionsEntity outgoingTx = PaymentTransactionsEntity.builder()
                .amount(200.0)
                .comment("Grocery shopping")
                .senderCardAccountId(account)
                .recipientAccountId(UUID.randomUUID())
                .transactionType(transferType)
                .transactionsStatus(completedStatus)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        outgoingTx = paymentTransactionsRepository.save(outgoingTx);
        account.getPaymentTransactionsEntities().add(outgoingTx);

        PaymentTransactionsEntity incomingTx2 = PaymentTransactionsEntity.builder()
                .amount(300.0)
                .comment("Freelance payment")
                .senderCardAccountId(createDummyCardAccount())
                .recipientAccountId(card.getId())
                .transactionType(transferType)
                .transactionsStatus(completedStatus)
                .createdAt(Instant.now().minus(3, ChronoUnit.HOURS))
                .build();
        incomingTx2 = paymentTransactionsRepository.save(incomingTx2);
        account.getPaymentTransactionsEntities().add(incomingTx2);

        cardAccountRepository.save(account);
    }

    private CardAccountEntity createDummyCardAccount() {
        CardAccountEntity dummyAccount = CardAccountEntity.builder()
                .currentBalance(1000.0)
                .updatedAt(Instant.now())
                .build();
        return cardAccountRepository.save(dummyAccount);
    }

    private void setupTransactionTypesAndStatuses() {
        transactionTypeRepository.findByTransactionsType(TransactionsType.TRANSFER)
                .orElseGet(() -> transactionTypeRepository.save(
                        TransactionTypeEntity.builder()
                                .transactionsType(TransactionsType.TRANSFER)
                                .build()));

        transactionsStatusRepository.findByTransactionsStatus(TransactionsStatusType.COMPLETED)
                .orElseGet(() -> transactionsStatusRepository.save(
                        TransactionsStatusEntity.builder()
                                .transactionsStatus(TransactionsStatusType.COMPLETED)
                                .build()));
    }

    @Test
    void checkBalance_Success() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        String responseJson = mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardStatusResponse response = objectMapper.readValue(responseJson, CardStatusResponse.class);

        assertNotNull(response);
        assertEquals(testCardBalance, response.getCurrentBalance());
        assertNotNull(response.getPaymentTransaction());
        assertEquals(3, response.getPaymentTransaction().size());

        CardStatusResponse.PaymentTransaction firstTx = response.getPaymentTransaction().get(0);
        assertNotNull(firstTx.getAmount());
        assertNotNull(firstTx.getComment());
        assertNotNull(firstTx.getType());
        assertNotNull(firstTx.getStatus());
        assertNotNull(firstTx.getCreatedAt());
    }

    @Test
    void checkBalance_MultipleCards_ShouldReturnCorrectBalance() throws Exception {
        String secondCardNumber = "8765432187654321";
        Double secondCardBalance = 2500.0;
        BankCardsEntity secondCard = createCardForUser(testUser, secondCardNumber, secondCardBalance);
        createTestTransactions(secondCard);

        when(securityFacade.checkCard(secondCardNumber)).thenReturn(true);
        when(securityFacade.findBankCardByNumber(secondCardNumber)).thenReturn(secondCard);

        CardNumberRequest request1 = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        String response1Json = mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardStatusResponse response1 = objectMapper.readValue(response1Json, CardStatusResponse.class);
        assertEquals(testCardBalance, response1.getCurrentBalance());

        CardNumberRequest request2 = CardNumberRequest.builder()
                .cardNumber(secondCardNumber)
                .build();

        String response2Json = mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardStatusResponse response2 = objectMapper.readValue(response2Json, CardStatusResponse.class);
        assertEquals(secondCardBalance, response2.getCurrentBalance());
    }

    @Test
    void checkBalance_CardWithNoTransactions_ShouldReturnEmptyList() throws Exception {
        String newCardNumber = "1111222233334444";
        Double newCardBalance = 500.0;
        BankCardsEntity newCard = createCardForUser(testUser, newCardNumber, newCardBalance);

        when(securityFacade.checkCard(newCardNumber)).thenReturn(true);
        when(securityFacade.findBankCardByNumber(newCardNumber)).thenReturn(newCard);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(newCardNumber)
                .build();

        String responseJson = mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardStatusResponse response = objectMapper.readValue(responseJson, CardStatusResponse.class);

        assertEquals(newCardBalance, response.getCurrentBalance());
        assertNotNull(response.getPaymentTransaction());
        assertTrue(response.getPaymentTransaction().isEmpty());
    }

    @Test
    void checkBalance_WithoutAdminRole_ShouldReturn403() throws Exception {
        UsersEntity userRoleOnly = UsersEntity.builder()
                .login("userOnly")
                .email("user@example.com")
                .password(passwordEncoder.encode("pass123"))
                .createdAt(Instant.now())
                .isActive(true)
                .build();
        userRoleOnly.setRoles(new HashSet<>());
        userRoleOnly.getRoles().add(userRole);
        usersRepository.save(userRoleOnly);

        String userOnlyToken = jwtCreatorConfig.createToken(userRoleOnly);

        when(securityFacade.getCurrentUser()).thenReturn(userRoleOnly);
        when(securityFacade.getLogin()).thenReturn(userRoleOnly.getLogin());

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkBalance_WithoutUserRole_ShouldReturn403() throws Exception {
        UsersEntity adminRoleOnly = UsersEntity.builder()
                .login("adminOnly")
                .email("admin@example.com")
                .password(passwordEncoder.encode("pass123"))
                .createdAt(Instant.now())
                .isActive(true)
                .build();
        adminRoleOnly.setRoles(new HashSet<>());
        adminRoleOnly.getRoles().add(adminRole);
        usersRepository.save(adminRoleOnly);

        String adminOnlyToken = jwtCreatorConfig.createToken(adminRoleOnly);

        when(securityFacade.getCurrentUser()).thenReturn(adminRoleOnly);
        when(securityFacade.getLogin()).thenReturn(adminRoleOnly.getLogin());

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + adminOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkBalance_NoAuthToken_ShouldReturn401() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(get("/api/user/cards/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkBalance_InvalidToken_ShouldReturn401() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer invalid_token_here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkBalance_CardWithNegativeBalance_ShouldWork() throws Exception {
        String negativeBalanceCardNumber = "9999888877776666";
        Double negativeBalance = -100.0;
        BankCardsEntity negativeCard = createCardForUser(testUser, negativeBalanceCardNumber, negativeBalance);

        when(securityFacade.checkCard(negativeBalanceCardNumber)).thenReturn(true);
        when(securityFacade.findBankCardByNumber(negativeBalanceCardNumber)).thenReturn(negativeCard);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(negativeBalanceCardNumber)
                .build();

        String responseJson = mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardStatusResponse response = objectMapper.readValue(responseJson, CardStatusResponse.class);
        assertEquals(negativeBalance, response.getCurrentBalance());
    }

    @Test
    void checkBalance_CardNotFound_ShouldReturnError() throws Exception {
        String nonExistentCardNumber = "0000000000000000";

        when(securityFacade.checkCard(nonExistentCardNumber)).thenReturn(false);
        when(securityFacade.findBankCardByNumber(nonExistentCardNumber)).thenReturn(null);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(nonExistentCardNumber)
                .build();

        mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void checkBalance_VerifyTransactionDetails() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        String responseJson = mockMvc.perform(get("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardStatusResponse response = objectMapper.readValue(responseJson, CardStatusResponse.class);
        assertFalse(response.getPaymentTransaction().isEmpty());

        for (CardStatusResponse.PaymentTransaction tx : response.getPaymentTransaction()) {
            assertNotNull(tx.getAmount());
            assertNotNull(tx.getComment());
            assertNotNull(tx.getType());
            assertEquals("TRANSFER", tx.getType());
            assertNotNull(tx.getStatus());
            assertEquals("COMPLETED", tx.getStatus());
            assertNotNull(tx.getCreatedAt());
        }
    }

    @Test
    void checkBalance_WrongHttpMethod_ShouldReturn405() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(post("/api/user/cards/balance")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isMethodNotAllowed());
    }
}