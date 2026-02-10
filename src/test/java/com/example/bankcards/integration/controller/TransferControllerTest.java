package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.crypto.AesEncryption;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CreateTransactionResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TransferControllerTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17")
    )
            .withDatabaseName("testdb")
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

    @MockBean
    private SecurityFacade securityFacade;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsersEntity userWithBothRoles;
    private RoleEntity adminRole;
    private RoleEntity userRole;
    private String userToken;
    
    private BankCardsEntity fromCard;
    private BankCardsEntity toCard;
    private String fromCardNumber = "1234567812345678";
    private String toCardNumber = "8765432187654321";
    private Double initialFromBalance = 1000.0;
    private Double initialToBalance = 500.0;
    private Double transferAmount = 200.0;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        paymentTransactionsRepository.deleteAll();
        bankCardsRepository.deleteAll();
        cardAccountRepository.deleteAll();
        usersRepository.deleteAll();

        userRole = roleRepository.findByRole(RoleType.USER)
                .stream()
                .findFirst()
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .role(RoleType.USER)
                        .build()));
        
        adminRole = roleRepository.findByRole(RoleType.ADMIN)
                .stream()
                .findFirst()
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .role(RoleType.ADMIN)
                        .build()));

        userWithBothRoles = createUserWithRoles("transferUser", "transfer@example.com", "pass123", adminRole, userRole);
        userToken = jwtCreatorConfig.createToken(userWithBothRoles);

        when(securityFacade.checkCard(anyString())).thenReturn(true);
        when(securityFacade.isCurrentActive()).thenReturn(true);
        when(securityFacade.getCurrentUser()).thenReturn(userWithBothRoles);
        when(securityFacade.getLogin()).thenReturn(userWithBothRoles.getLogin());
        
        // Добавьте настройку для findBankCardByNumber
        when(securityFacade.findBankCardByNumber(fromCardNumber)).thenReturn(fromCard);
        when(securityFacade.findBankCardByNumber(toCardNumber)).thenReturn(toCard);
        
        fromCard = createCardForUser(userWithBothRoles, fromCardNumber, initialFromBalance);
        toCard = createCardForUser(userWithBothRoles, toCardNumber, initialToBalance);
        
        // Перенастройте моки после создания карт
        when(securityFacade.findBankCardByNumber(fromCardNumber)).thenReturn(fromCard);
        when(securityFacade.findBankCardByNumber(toCardNumber)).thenReturn(toCard);

        setupTransactionTypesAndStatuses();
    }

    private UsersEntity createUserWithRoles(String login, String email, String password, RoleEntity... roles) {
        UsersEntity user = UsersEntity.builder()
                .login(login)
                .email(email)
                .password(passwordEncoder.encode(password))
                .createdAt(Instant.now())
                .build();

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        for (RoleEntity role : roles) {
            user.getRoles().add(role);
        }

        return usersRepository.save(user);
    }

    private BankCardsEntity createCardForUser(UsersEntity user, String cardNumber, Double initialBalance) {
        CardAccountEntity cardAccount = CardAccountEntity.builder()
                .currentBalance(initialBalance)
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

    private void setupTransactionTypesAndStatuses() {
        TransactionTypeEntity transferType = transactionTypeRepository.findByTransactionsType(
                TransactionsType.TRANSFER
        ).orElseGet(() -> { return transactionTypeRepository.save(
                TransactionTypeEntity.builder()
                    .transactionsType(TransactionsType.TRANSFER)
                    .build());
        });

        TransactionsStatusEntity completedStatus = transactionsStatusRepository.findByTransactionsStatus(
                TransactionsStatusType.COMPLETED
        ).orElseGet(() -> transactionsStatusRepository.save(
                TransactionsStatusEntity.builder()
                        .transactionsStatus(TransactionsStatusType.COMPLETED)
                        .build()
        ));
    }

    @Test
    void transfer_Success() throws Exception {
        TransferRequest transferRequest = TransferRequest.builder()
                .fromNumberCard(fromCardNumber)
                .toNumberCard(toCardNumber)
                .amount(transferAmount)
                .comment("Test transfer")
                .transactionsType(TransactionsType.TRANSFER)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CreateTransactionResponse response = objectMapper.readValue(responseJson, CreateTransactionResponse.class);
        
        assertNotNull(response);
        assertEquals(fromCard.getId(), response.getFromCardAccountId());
        assertEquals(toCard.getId(), response.getToCardAccountId());
        assertEquals("TRANSFER", response.getTransactionType());
        assertEquals(transferAmount, response.getAmount());

        CardAccountEntity updatedFromAccount = cardAccountRepository.findById(fromCard.getCardAccountEntity().getId()).orElseThrow();
        CardAccountEntity updatedToAccount = cardAccountRepository.findById(toCard.getCardAccountEntity().getId()).orElseThrow();

        assertEquals(initialFromBalance - transferAmount, updatedFromAccount.getCurrentBalance());
        assertEquals(initialToBalance + transferAmount, updatedToAccount.getCurrentBalance());

        assertEquals(1, updatedFromAccount.getPaymentTransactionsEntities().size());
        assertEquals(1, updatedToAccount.getPaymentTransactionsEntities().size());
        
        PaymentTransactionsEntity transaction = paymentTransactionsRepository.findAll().get(0);
        assertEquals(transferAmount, transaction.getAmount());
        assertEquals("Test transfer", transaction.getComment());
        assertEquals(fromCard.getCardAccountEntity().getId(), transaction.getSenderCardAccountId().getId());
        assertEquals(toCard.getId(), transaction.getRecipientAccountId());
    }


    @Test
    void transfer_MultipleTransactions_Success() throws Exception {
        TransferRequest transferRequest1 = TransferRequest.builder()
                .fromNumberCard(fromCardNumber)
                .toNumberCard(toCardNumber)
                .amount(100.0)
                .comment("First transfer")
                .transactionsType(TransactionsType.TRANSFER)
                .build();

        TransferRequest transferRequest2 = TransferRequest.builder()
                .fromNumberCard(fromCardNumber)
                .toNumberCard(toCardNumber)
                .amount(150.0)
                .comment("Second transfer")
                .transactionsType(TransactionsType.TRANSFER)
                .build();

        mockMvc.perform(post("/api/user/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest2)))
                .andExpect(status().isOk());

        CardAccountEntity updatedFromAccount = cardAccountRepository.findById(fromCard.getCardAccountEntity().getId()).orElseThrow();
        CardAccountEntity updatedToAccount = cardAccountRepository.findById(toCard.getCardAccountEntity().getId()).orElseThrow();

        assertEquals(initialFromBalance - 250.0, updatedFromAccount.getCurrentBalance());
        assertEquals(initialToBalance + 250.0, updatedToAccount.getCurrentBalance());
        assertEquals(2, updatedFromAccount.getPaymentTransactionsEntities().size());
        assertEquals(2, updatedToAccount.getPaymentTransactionsEntities().size());
    }

    @Test
    void transfer_WithoutAdminRole_ShouldReturn403() throws Exception {
        UsersEntity userRoleOnly = createUserWithRoles("userOnly", "user@example.com", "pass123", userRole);
        String userOnlyToken = jwtCreatorConfig.createToken(userRoleOnly);

        TransferRequest transferRequest = TransferRequest.builder()
                .fromNumberCard(fromCardNumber)
                .toNumberCard(toCardNumber)
                .amount(transferAmount)
                .comment("No admin role")
                .transactionsType(TransactionsType.TRANSFER)
                .build();

        mockMvc.perform(post("/api/user/cards/transfer")
                        .header("Authorization", "Bearer " + userOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void transfer_WithoutUserRole_ShouldReturn403() throws Exception {
        UsersEntity adminRoleOnly = createUserWithRoles("adminOnly", "admin@example.com", "pass123", adminRole);
        String adminOnlyToken = jwtCreatorConfig.createToken(adminRoleOnly);

        TransferRequest transferRequest = TransferRequest.builder()
                .fromNumberCard(fromCardNumber)
                .toNumberCard(toCardNumber)
                .amount(transferAmount)
                .comment("No user role")
                .transactionsType(TransactionsType.TRANSFER)
                .build();

        mockMvc.perform(post("/api/user/cards/transfer")
                        .header("Authorization", "Bearer " + adminOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void transfer_SameCard_ShouldWork() throws Exception {
        TransferRequest transferRequest = TransferRequest.builder()
                .fromNumberCard(fromCardNumber)
                .toNumberCard(fromCardNumber)
                .amount(100.0)
                .comment("Transfer to same card")
                .transactionsType(TransactionsType.TRANSFER)
                .build();

        mockMvc.perform(post("/api/user/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk());

        CardAccountEntity updatedAccount = cardAccountRepository.findById(fromCard.getCardAccountEntity().getId()).orElseThrow();
        assertEquals(initialFromBalance, updatedAccount.getCurrentBalance());
    }

    private BankCardsEntity createExpiredCard(UsersEntity user, String cardNumber, Double balance) {
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
                .createdAt(Instant.now().minus(365 * 6, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .isActive(true)
                .user(user)
                .cardAccountEntity(cardAccount)
                .build();

        cardAccount.setBankCardsEntity(card);
        return bankCardsRepository.save(card);
    }

    private BankCardsEntity createInactiveCard(UsersEntity user, String cardNumber, Double balance) {
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
                .isActive(false)
                .user(user)
                .cardAccountEntity(cardAccount)
                .build();

        cardAccount.setBankCardsEntity(card);
        return bankCardsRepository.save(card);
    }
}