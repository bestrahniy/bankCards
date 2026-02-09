package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.util.AesEncryption;
import com.example.bankcards.util.AesHelper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
public class CardCreateTest {

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
    private AesHelper aesHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsersEntity adminUser;

    private UsersEntity regularUser;

    private RoleEntity adminRole;

    private RoleEntity userRole;

    private String adminToken;

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

        adminUser = createUserWithRoles("admin", "admin@example.com", "admin123", adminRole, userRole);

        regularUser = createUserWithRoles("regularUser", "user@example.com", "user123", userRole);

        adminToken = jwtCreatorConfig.createToken(adminUser);

        when(aesHelper.getMaskedCardNumber(anyString())).thenReturn("**** **** **** 1234");
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

    @Test
    void createCard_Success() throws Exception {
        UUID userId = regularUser.getId();

        mockMvc.perform(post("/api/admin/card/create/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(regularUser.getLogin()))
                .andExpect(jsonPath("$.numberCard").value("**** **** **** 1234"));

        List<BankCardsEntity> createdCard = bankCardsRepository.findByUser(regularUser);

        BankCardsEntity card = createdCard.getFirst();
        assertEquals(regularUser.getId(), card.getUser().getId());
        assertNotNull(card.getCardAccountEntity());
        assertNotNull(card.getNumber());
        assertNotNull(card.getCvc2());
        assertTrue(card.getCvc2() >= 100 && card.getCvc2() <= 999);
        assertTrue(card.getExpiresAt().isAfter(card.getCreatedAt()));
    }

    @Test
    void createCard_WithoutAdminRole_ShouldReturn403() throws Exception {
        UsersEntity nonAdminUser = createUserWithRoles("nonadmin", "nonadmin@example.com", "pass123", userRole);
        String nonAdminToken = jwtCreatorConfig.createToken(nonAdminUser);
        UUID userId = regularUser.getId();

        mockMvc.perform(post("/api/admin/card/create/{userId}", userId)
                        .header("Authorization", "Bearer " + nonAdminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCard_EncryptedCardNumber_Success() throws Exception {
        UUID userId = regularUser.getId();

        mockMvc.perform(post("/api/admin/card/create/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<BankCardsEntity> createdCard = bankCardsRepository.findByUser(regularUser);

        BankCardsEntity card = createdCard.getLast();
        String encryptedNumber = card.getNumber().toString();
        
        assertNotNull(encryptedNumber);
        
        String decryptedNumber = aesEncryption.decrypt(encryptedNumber);
        assertNotNull(decryptedNumber);
        
        assertTrue(decryptedNumber.matches("\\d+"));
        assertEquals(16, decryptedNumber.length());
    }

    @Test
    void createCard_CardAccountCreated_Success() throws Exception {
        UUID userId = regularUser.getId();

        mockMvc.perform(post("/api/admin/card/create/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<BankCardsEntity> createdCard = bankCardsRepository.findByUser(regularUser);

        BankCardsEntity card = createdCard.getLast();
        CardAccountEntity cardAccount = card.getCardAccountEntity();
        
        assertNotNull(cardAccount);
        assertNotNull(cardAccount.getId());
        assertEquals(card, cardAccount.getBankCardsEntity());
        
        Optional<CardAccountEntity> savedCardAccount = cardAccountRepository.findById(cardAccount.getId());
        assertTrue(savedCardAccount.isPresent());
    }

    @Test
    void createCard_ResponseFormat_Correct() throws Exception {
        UUID userId = regularUser.getId();

        String response = mockMvc.perform(post("/api/admin/card/create/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CreateCardResponse createCardResponse = objectMapper.readValue(response, CreateCardResponse.class);
        assertEquals(regularUser.getLogin(), createCardResponse.getLogin());
        assertEquals("**** **** **** 1234", createCardResponse.getNumberCard());
    }

    @Test
    void createCard_InvalidToken_ShouldReturn401() throws Exception {
        UUID userId = regularUser.getId();
        String invalidToken = "invalid.token.here";

        mockMvc.perform(post("/api/admin/card/create/{userId}", userId)
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCard_NoToken_ShouldReturn401() throws Exception {
        UUID userId = regularUser.getId();

        mockMvc.perform(post("/api/admin/card/create/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCard_AdminHasMultipleRoles_Success() throws Exception {
        UsersEntity userWithMultipleRoles = createUserWithRoles("multi", "multi@example.com", "pass123", adminRole, userRole);
        String multiRoleToken = jwtCreatorConfig.createToken(userWithMultipleRoles);
        UUID userId = regularUser.getId();

        mockMvc.perform(post("/api/admin/card/create/{userId}", userId)
                        .header("Authorization", "Bearer " + multiRoleToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}