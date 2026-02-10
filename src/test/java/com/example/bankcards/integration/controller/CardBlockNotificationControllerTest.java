package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.crypto.AesEncryption;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.EventType;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.NotificationRepository;
import com.example.bankcards.repository.RoleRepository;
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
public class CardBlockNotificationControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17")
    )
            .withDatabaseName("block_card_test_db")
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
    private NotificationRepository notificationRepository;

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

    @BeforeEach
    void setUp() throws IllegalAccessException {
        notificationRepository.deleteAll();
        bankCardsRepository.deleteAll();
        cardAccountRepository.deleteAll();
        usersRepository.deleteAll();

        userRole = roleRepository.findByRole(RoleType.USER)
                .stream()
                .findFirst()
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .role(RoleType.USER)
                        .isActive(true)
                        .build()));
        
        adminRole = roleRepository.findByRole(RoleType.ADMIN)
                .stream()
                .findFirst()
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .role(RoleType.ADMIN)
                        .isActive(true)
                        .build()));

        testUser = createUserWithRoles("testUser", "test@example.com", "password123", adminRole, userRole);

        userToken = jwtCreatorConfig.createToken(testUser);

        testCard = createCardForUser(testUser, testCardNumber, 1000.0);

        when(securityFacade.checkCard(anyString())).thenReturn(true);
        when(securityFacade.isCurrentActive()).thenReturn(true);
        when(securityFacade.getCurrentUser()).thenReturn(testUser);
        when(securityFacade.getLogin()).thenReturn(testUser.getLogin());
        when(securityFacade.findBankCardByNumber(testCardNumber)).thenReturn(testCard);
    }

    private UsersEntity createUserWithRoles(String login, String email, String password, RoleEntity... roles) {
        UsersEntity user = UsersEntity.builder()
                .login(login)
                .email(email)
                .password(passwordEncoder.encode(password))
                .createdAt(Instant.now())
                .isActive(true)
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

    @Test
    void blockCard_Success() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        NotificationResponse response = objectMapper.readValue(responseJson, NotificationResponse.class);
        
        assertNotNull(response);
        assertEquals(testUser.getLogin(), response.getLogin());
        assertNotNull(response.getNotification());
        assertNotNull(response.getNotification().getId());
        assertEquals(EventType.BLOCK_CARD.toString(), response.getNotification().getEvent());
        assertNotNull(response.getNotification().getCreatedAt());
        assertTrue(response.getNotification().getIsActive());
        
        List<NotificationEntity> notifications = notificationRepository.findAll();
        assertEquals(1, notifications.size());
        
        NotificationEntity savedNotification = notifications.get(0);
        assertEquals(testUser.getId(), savedNotification.getUser().getId());
        assertNotNull(savedNotification.getCard());
        assertEquals(testCard.getCardAccountEntity().getId(), savedNotification.getCard().getId());
        assertEquals(EventType.BLOCK_CARD, savedNotification.getEvent());
        assertTrue(savedNotification.isActive());
    }

    @Test
    void blockCard_MultipleRequests_Success() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<NotificationEntity> notifications = notificationRepository.findAll();
        assertEquals(2, notifications.size());
        
        assertTrue(notifications.get(0).isActive());
        assertTrue(notifications.get(1).isActive());
        
        assertEquals(testUser.getId(), notifications.get(0).getUser().getId());
        assertEquals(testUser.getId(), notifications.get(1).getUser().getId());
        assertNotNull(notifications.get(0).getCard());
        assertNotNull(notifications.get(1).getCard());
        assertEquals(testCard.getCardAccountEntity().getId(), notifications.get(0).getCard().getId());
        assertEquals(testCard.getCardAccountEntity().getId(), notifications.get(1).getCard().getId());
    }

    @Test
    void blockCard_WithoutAdminRole_ShouldReturn403() throws Exception {
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

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        when(securityFacade.getCurrentUser()).thenReturn(userRoleOnly);
        when(securityFacade.getLogin()).thenReturn(userRoleOnly.getLogin());

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void blockCard_WithoutUserRole_ShouldReturn403() throws Exception {
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

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        when(securityFacade.getCurrentUser()).thenReturn(adminRoleOnly);
        when(securityFacade.getLogin()).thenReturn(adminRoleOnly.getLogin());

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + adminOnlyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void blockCard_NoAuthToken_ShouldReturn401() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(post("/api/user/cards/request/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blockCard_InvalidToken_ShouldReturn401() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer invalid_token_here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blockCard_DifferentCards_Success() throws Exception {
        String secondCardNumber = "8765432187654321";
        BankCardsEntity secondCard = createCardForUser(testUser, secondCardNumber, 2000.0);

        when(securityFacade.findBankCardByNumber(secondCardNumber)).thenReturn(secondCard);

        CardNumberRequest request1 = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        String response1Json = mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        NotificationResponse response1 = objectMapper.readValue(response1Json, NotificationResponse.class);
        assertEquals(EventType.BLOCK_CARD.toString(), response1.getNotification().getEvent());

        CardNumberRequest request2 = CardNumberRequest.builder()
                .cardNumber(secondCardNumber)
                .build();

        String response2Json = mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        NotificationResponse response2 = objectMapper.readValue(response2Json, NotificationResponse.class);
        assertEquals(EventType.BLOCK_CARD.toString(), response2.getNotification().getEvent());

        List<NotificationEntity> notifications = notificationRepository.findAll();
        assertEquals(2, notifications.size());
        assertNotNull(notifications.get(0).getCard());
        assertNotNull(notifications.get(1).getCard());
        assertNotEquals(notifications.get(0).getCard().getId(), notifications.get(1).getCard().getId());
    }

    @Test
    void blockCard_ExpiredCard_ShouldStillCreateNotification() throws Exception {
        String expiredCardNumber = "5555666677778888";
        BankCardsEntity expiredCard = createExpiredCard(testUser, expiredCardNumber, 1000.0);

        when(securityFacade.findBankCardByNumber(expiredCardNumber)).thenReturn(expiredCard);
        when(securityFacade.checkCard(expiredCardNumber)).thenReturn(false);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(expiredCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        NotificationResponse response = objectMapper.readValue(responseJson, NotificationResponse.class);
        assertEquals(EventType.BLOCK_CARD.toString(), response.getNotification().getEvent());

        List<NotificationEntity> notifications = notificationRepository.findAll();
        assertEquals(1, notifications.size());
        assertNotNull(notifications.get(0).getCard());
        assertEquals(expiredCard.getCardAccountEntity().getId(), notifications.get(0).getCard().getId());
    }

    @Test
    void blockCard_InactiveCard_ShouldStillCreateNotification() throws Exception {
        String inactiveCardNumber = "4444333322221111";
        BankCardsEntity inactiveCard = createInactiveCard(testUser, inactiveCardNumber, 1000.0);

        when(securityFacade.findBankCardByNumber(inactiveCardNumber)).thenReturn(inactiveCard);
        when(securityFacade.checkCard(inactiveCardNumber)).thenReturn(false);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(inactiveCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        NotificationResponse response = objectMapper.readValue(responseJson, NotificationResponse.class);
        assertEquals(EventType.BLOCK_CARD.toString(), response.getNotification().getEvent());

        List<NotificationEntity> notifications = notificationRepository.findAll();
        assertEquals(1, notifications.size());
        assertNotNull(notifications.get(0).getCard());
        assertEquals(inactiveCard.getCardAccountEntity().getId(), notifications.get(0).getCard().getId());
    }

    @Test
    void blockCard_WrongHttpMethod_ShouldReturn405() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(get("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void blockCard_VerifyNotificationLinkedToCardAccount() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        NotificationEntity notification = notificationRepository.findAll().get(0);

        assertNotNull(notification.getCard());
        assertEquals(testCard.getCardAccountEntity().getId(), notification.getCard().getId());
        assertEquals(testUser.getId(), notification.getUser().getId());

        CardAccountEntity cardAccount = cardAccountRepository.findById(testCard.getCardAccountEntity().getId()).orElseThrow();
        assertFalse(cardAccount.getNotificationEntities().isEmpty());
        assertTrue(cardAccount.getNotificationEntities().contains(notification));
    }

    @Test
    void blockCard_CardWithExistingNotifications_ShouldAddNewNotification() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        CardAccountEntity cardAccount = cardAccountRepository.findById(testCard.getCardAccountEntity().getId()).orElseThrow();
        assertEquals(2, cardAccount.getNotificationEntities().size());

        for (NotificationEntity notification : cardAccount.getNotificationEntities()) {
            assertEquals(EventType.BLOCK_CARD, notification.getEvent());
        }
    }

    @Test
    void blockCard_UserTriesToBlockOtherUsersCard_ShouldStillCreateNotification() throws Exception {
        UsersEntity anotherUser = UsersEntity.builder()
                .login("anotherUser")
                .email("another@example.com")
                .password(passwordEncoder.encode("pass123"))
                .createdAt(Instant.now())
                .isActive(true)
                .build();
        anotherUser.setRoles(new HashSet<>());
        anotherUser.getRoles().add(adminRole);
        anotherUser.getRoles().add(userRole);
        usersRepository.save(anotherUser);

        String anotherUserCardNumber = "9999888877776666";
        BankCardsEntity anotherUserCard = createCardForUser(anotherUser, anotherUserCardNumber, 2000.0);

        when(securityFacade.findBankCardByNumber(anotherUserCardNumber)).thenReturn(anotherUserCard);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(anotherUserCardNumber)
                .build();

        mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<NotificationEntity> notifications = notificationRepository.findAll();
        assertEquals(1, notifications.size());
        assertEquals(anotherUserCard.getCardAccountEntity().getId(), notifications.get(0).getCard().getId());
        assertEquals(testUser.getId(), notifications.get(0).getUser().getId());
    }

    @Test
    void blockCard_NotificationHasCorrectTimestamp() throws Exception {
        Instant beforeRequest = Instant.now().minusSeconds(1);
        
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(testCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/cards/request/block")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Instant afterRequest = Instant.now().plusSeconds(1);
        
        NotificationResponse response = objectMapper.readValue(responseJson, NotificationResponse.class);

        assertNotNull(response.getNotification().getCreatedAt());
        assertTrue(response.getNotification().getCreatedAt().isAfter(beforeRequest));
        assertTrue(response.getNotification().getCreatedAt().isBefore(afterRequest));
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