package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardActiveStatusResponse;
import com.example.bankcards.facade.SecurityFacade;
import com.example.bankcards.model.entity.*;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.*;
import com.example.bankcards.util.AesEncryption;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CardBlockUnblockControllerTest {

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
    private AesEncryption aesEncryption;

    @Autowired
    private JwtCreatorConfigTest jwtCreatorConfig;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private SecurityFacade securityFacade;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsersEntity adminUser;
    private UsersEntity regularUser;
    private RoleEntity adminRole;
    private RoleEntity userRole;
    private String adminToken;
    private String regularUserToken;
    
    private BankCardsEntity activeCard;
    private BankCardsEntity blockedCard;
    private String activeCardNumber = "1234567812345678";
    private String blockedCardNumber = "8765432187654321";

    @BeforeEach
    void setUp() {
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

        adminUser = createUserWithRoles("adminUser", "admin@example.com", "admin123", adminRole);
        regularUser = createUserWithRoles("regularUser", "user@example.com", "user123", userRole);

        adminToken = jwtCreatorConfig.createToken(adminUser);
        regularUserToken = jwtCreatorConfig.createToken(regularUser);

        activeCard = createCardForUser(adminUser, activeCardNumber, true);
        blockedCard = createCardForUser(adminUser, blockedCardNumber, false);

        when(securityFacade.findBankCardByNumber(activeCardNumber)).thenReturn(activeCard);
        when(securityFacade.findBankCardByNumber(blockedCardNumber)).thenReturn(blockedCard);
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

    private BankCardsEntity createCardForUser(UsersEntity user, String cardNumber, boolean isActive) {
        CardAccountEntity cardAccount = CardAccountEntity.builder()
                .currentBalance(1000.0)
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
                .isActive(isActive)
                .user(user)
                .cardAccountEntity(cardAccount)
                .build();

        cardAccount.setBankCardsEntity(card);
        return bankCardsRepository.save(card);
    }

    @Test
    void blockCard_Success() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(activeCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/admin/card/block")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse response = objectMapper.readValue(responseJson, CardActiveStatusResponse.class);
        
        assertNotNull(response);
        assertEquals("**** **** **** 5678", response.getCardNumber());
        assertFalse(response.getIsActive());

        BankCardsEntity updatedCard = bankCardsRepository.findById(activeCard.getId()).orElseThrow();
        assertFalse(updatedCard.getIsActive());
    }

    @Test
    void blockCard_AlreadyBlocked_ShouldStillSucceed() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(blockedCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/admin/card/block")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse response = objectMapper.readValue(responseJson, CardActiveStatusResponse.class);
        
        assertNotNull(response);
        assertFalse(response.getIsActive());

        BankCardsEntity updatedCard = bankCardsRepository.findById(blockedCard.getId()).orElseThrow();
        assertFalse(updatedCard.getIsActive());
    }

    @Test
    void unblockCard_Success() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(blockedCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/admin/card/unblock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse response = objectMapper.readValue(responseJson, CardActiveStatusResponse.class);
        
        assertNotNull(response);
        assertEquals("**** **** **** 4321", response.getCardNumber());
        assertTrue(response.getIsActive());

        BankCardsEntity updatedCard = bankCardsRepository.findById(blockedCard.getId()).orElseThrow();
        assertTrue(updatedCard.getIsActive());
    }

    @Test
    void unblockCard_AlreadyActive_ShouldStillSucceed() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(activeCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/admin/card/unblock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse response = objectMapper.readValue(responseJson, CardActiveStatusResponse.class);
        
        assertNotNull(response);
        assertTrue(response.getIsActive());

        BankCardsEntity updatedCard = bankCardsRepository.findById(activeCard.getId()).orElseThrow();
        assertTrue(updatedCard.getIsActive());
    }

    @Test
    void blockCard_WithoutAdminRole_ShouldReturn403() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(activeCardNumber)
                .build();

        mockMvc.perform(post("/api/admin/card/block")
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unblockCard_WithoutAdminRole_ShouldReturn403() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(blockedCardNumber)
                .build();

        mockMvc.perform(post("/api/admin/card/unblock")
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }


    @Test
    void blockCard_ExpiredCard_ShouldStillBlock() throws Exception {
        BankCardsEntity expiredCard = createExpiredCard(adminUser, "4444555566667777");
        String expiredCardNumber = "4444555566667777";
        
        when(securityFacade.findBankCardByNumber(expiredCardNumber)).thenReturn(expiredCard);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(expiredCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/admin/card/block")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse response = objectMapper.readValue(responseJson, CardActiveStatusResponse.class);
        
        assertNotNull(response);
        assertFalse(response.getIsActive());

        BankCardsEntity updatedCard = bankCardsRepository.findById(expiredCard.getId()).orElseThrow();
        assertFalse(updatedCard.getIsActive());
    }

    @Test
    void unblockCard_ExpiredCard_ShouldStillUnblock() throws Exception {
        BankCardsEntity expiredBlockedCard = createExpiredCard(adminUser, "5555666677778888");
        expiredBlockedCard.setIsActive(false);
        bankCardsRepository.save(expiredBlockedCard);
        
        String expiredCardNumber = "5555666677778888";
        
        when(securityFacade.findBankCardByNumber(expiredCardNumber)).thenReturn(expiredBlockedCard);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(expiredCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/admin/card/unblock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse response = objectMapper.readValue(responseJson, CardActiveStatusResponse.class);
        
        assertNotNull(response);
        assertTrue(response.getIsActive());

        BankCardsEntity updatedCard = bankCardsRepository.findById(expiredBlockedCard.getId()).orElseThrow();
        assertTrue(updatedCard.getIsActive());
    }

    @Test
    void blockCard_ForDifferentUser_Success() throws Exception {
        BankCardsEntity anotherUserCard = createCardForUser(regularUser, "9999888877776666", true);
        String anotherUserCardNumber = "9999888877776666";
        
        when(securityFacade.findBankCardByNumber(anotherUserCardNumber)).thenReturn(anotherUserCard);

        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(anotherUserCardNumber)
                .build();

        String responseJson = mockMvc.perform(post("/api/admin/card/block")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse response = objectMapper.readValue(responseJson, CardActiveStatusResponse.class);
        
        assertNotNull(response);
        assertFalse(response.getIsActive());

        BankCardsEntity updatedCard = bankCardsRepository.findById(anotherUserCard.getId()).orElseThrow();
        assertFalse(updatedCard.getIsActive());
    }

    @Test
    void blockUnblockCycle_Success() throws Exception {
        CardNumberRequest blockRequest = CardNumberRequest.builder()
                .cardNumber(activeCardNumber)
                .build();

        String blockResponseJson = mockMvc.perform(post("/api/admin/card/block")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse blockResponse = objectMapper.readValue(blockResponseJson, CardActiveStatusResponse.class);
        assertFalse(blockResponse.getIsActive());

        CardNumberRequest unblockRequest = CardNumberRequest.builder()
                .cardNumber(activeCardNumber)
                .build();

        String unblockResponseJson = mockMvc.perform(post("/api/admin/card/unblock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unblockRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CardActiveStatusResponse unblockResponse = objectMapper.readValue(unblockResponseJson, CardActiveStatusResponse.class);
        assertTrue(unblockResponse.getIsActive());

        BankCardsEntity finalCard = bankCardsRepository.findById(activeCard.getId()).orElseThrow();
        assertTrue(finalCard.getIsActive());
    }

    @Test
    void blockCard_NoToken_ShouldReturn401() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(activeCardNumber)
                .build();

        mockMvc.perform(post("/api/admin/card/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unblockCard_NoToken_ShouldReturn401() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(blockedCardNumber)
                .build();

        mockMvc.perform(post("/api/admin/card/unblock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blockCard_InvalidToken_ShouldReturn401() throws Exception {
        CardNumberRequest request = CardNumberRequest.builder()
                .cardNumber(activeCardNumber)
                .build();

        mockMvc.perform(post("/api/admin/card/block")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private BankCardsEntity createExpiredCard(UsersEntity user, String cardNumber) {
        CardAccountEntity cardAccount = CardAccountEntity.builder()
                .currentBalance(1000.0)
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
}