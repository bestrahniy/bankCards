package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.CardAccountEntity;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.BankCardsRepository;
import com.example.bankcards.repository.CardAccountRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.util.AesHelper;
import com.fasterxml.jackson.core.type.TypeReference;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CardPaginationTest {

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
    private JwtCreatorConfigTest jwtCreatorConfig;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private AesHelper aesHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsersEntity adminUser;
    private UsersEntity regularUser1;
    private UsersEntity regularUser2;
    private RoleEntity adminRole;
    private RoleEntity userRole;
    private String adminToken;
    private String regularUser1Token;

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

        adminUser = createUserWithRoles("adminUser", "admin@example.com", "admin123", adminRole, userRole);
        regularUser1 = createUserWithRoles("regularUser1", "user1@example.com", "user123", userRole);
        regularUser2 = createUserWithRoles("regularUser2", "user2@example.com", "user123", userRole);

        adminToken = jwtCreatorConfig.createToken(adminUser);
        regularUser1Token = jwtCreatorConfig.createToken(regularUser1);

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

    private BankCardsEntity createCardForUser(UsersEntity user, boolean active, Instant createdAt) {
        CardAccountEntity cardAccount = CardAccountEntity.builder()
                .currentBalance(0.00)
                .updatedAt(Instant.now())
                .build();
        cardAccount = cardAccountRepository.save(cardAccount);

        BankCardsEntity card = BankCardsEntity.builder()
                .number("encrypted-card-" + UUID.randomUUID())
                .cvc2(123)
                .createdAt(createdAt)
                .expiresAt(createdAt.plus(365 * 5, ChronoUnit.DAYS))
                .isActive(true)
                .user(user)
                .cardAccountEntity(cardAccount)
                .build();

        cardAccount.setBankCardsEntity(card);
        return bankCardsRepository.save(card);
    }

    @Test
    void showAllCards_Pagination_FirstPage() throws Exception {
        Instant now = Instant.now();
        
        for (int i = 0; i < 15; i++) {
            createCardForUser(adminUser, true, now.minus(i, ChronoUnit.DAYS));
        }

        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        
        assertEquals(0, pageResponse.getPage());
        assertEquals(10, pageResponse.getSize());
        assertEquals(15, pageResponse.getTotalElements());
        assertEquals(2, pageResponse.getTotalPages());
        assertFalse(pageResponse.isLast());
        assertEquals(10, pageResponse.getContent().size());
    }

    @Test
    void showAllCards_Pagination_SecondPage() throws Exception {
        Instant now = Instant.now();
        
        for (int i = 0; i < 15; i++) {
            createCardForUser(adminUser, true, now.minus(i, ChronoUnit.DAYS));
        }

        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        
        assertEquals(1, pageResponse.getPage());
        assertEquals(10, pageResponse.getSize());
        assertEquals(15, pageResponse.getTotalElements());
        assertEquals(2, pageResponse.getTotalPages());
        assertTrue(pageResponse.isLast());
        assertEquals(5, pageResponse.getContent().size());
    }

    @Test
    void showAllCards_Pagination_EmptyPage() throws Exception {
        Instant now = Instant.now();
        
        for (int i = 0; i < 5; i++) {
            createCardForUser(adminUser, true, now.minus(i, ChronoUnit.DAYS));
        }

        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "2")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        
        assertEquals(2, pageResponse.getPage());
        assertEquals(10, pageResponse.getSize());
        assertEquals(5, pageResponse.getTotalElements());
        assertEquals(1, pageResponse.getTotalPages());
        assertTrue(pageResponse.isLast());
        assertEquals(0, pageResponse.getContent().size());
    }

    @Test
    void showAllCards_SortedByCreatedAtDesc() throws Exception {
        Instant now = Instant.now();
        
        BankCardsEntity oldestCard = createCardForUser(adminUser, true, now.minus(10, ChronoUnit.DAYS));
        BankCardsEntity newestCard = createCardForUser(adminUser, true, now);
        BankCardsEntity middleCard = createCardForUser(adminUser, true, now.minus(5, ChronoUnit.DAYS));

        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        
        List<CardResponse> content = pageResponse.getContent();
        assertEquals(3, content.size());
        
        assertTrue(content.get(0).getExpiredAt().isAfter(content.get(1).getExpiredAt()));
        assertTrue(content.get(1).getExpiredAt().isAfter(content.get(2).getExpiredAt()));
    }

    @Test
    void showAllCards_UserSeesOnlyTheirCards() throws Exception {
        Instant now = Instant.now();
        
        createCardForUser(adminUser, true, now.minus(1, ChronoUnit.DAYS));
        createCardForUser(adminUser, true, now.minus(2, ChronoUnit.DAYS));
        createCardForUser(regularUser1, true, now.minus(3, ChronoUnit.DAYS));
        createCardForUser(regularUser2, true, now.minus(4, ChronoUnit.DAYS));

        when(aesHelper.getMaskedCardNumber(anyString())).thenReturn("**** **** **** 1234");

        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        
        assertEquals(2, pageResponse.getTotalElements());
        pageResponse.getContent().forEach(cardResponse -> {
            assertEquals(adminUser.getLogin(), cardResponse.getLogin());
        });
    }

    @Test
    void showAllCards_DefaultPagination() throws Exception {
        Instant now = Instant.now();
        
        for (int i = 0; i < 25; i++) {
            createCardForUser(adminUser, true, now.minus(i, ChronoUnit.DAYS));
        }

        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        
        assertEquals(0, pageResponse.getPage());
        assertEquals(20, pageResponse.getSize());
        assertEquals(25, pageResponse.getTotalElements());
        assertEquals(2, pageResponse.getTotalPages());
        assertEquals(20, pageResponse.getContent().size());
    }

    @Test
    void showAllCards_InvalidPage_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void showAllCards_InvalidSize_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void showAllCards_WithoutAdminRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + regularUser1Token)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void showAllCards_UserWithBothRoles_Success() throws Exception {
        UsersEntity userWithBothRoles = createUserWithRoles("bothroles", "both@example.com", "pass123", adminRole, userRole);
        String bothRolesToken = jwtCreatorConfig.createToken(userWithBothRoles);
        
        Instant now = Instant.now();
        createCardForUser(userWithBothRoles, true, now.minus(1, ChronoUnit.DAYS));
        createCardForUser(userWithBothRoles, true, now.minus(2, ChronoUnit.DAYS));

        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + bothRolesToken)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        assertEquals(2, pageResponse.getTotalElements());
    }

    @Test
    void showAllCards_NoCards_EmptyResponse() throws Exception {
        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        
        assertEquals(0, pageResponse.getTotalElements());
        assertEquals(0, pageResponse.getTotalPages());
        assertTrue(pageResponse.getContent().isEmpty());
        assertTrue(pageResponse.isLast());
    }

    @Test
    void showAllCards_MaxSizeParameter() throws Exception {
        Instant now = Instant.now();
        
        for (int i = 0; i < 50; i++) {
            createCardForUser(adminUser, true, now.minus(i, ChronoUnit.DAYS));
        }

        String response = mockMvc.perform(get("/api/user/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageResponse<CardResponse> pageResponse = objectMapper.readValue(response, new TypeReference<PageResponse<CardResponse>>() {});
        
        assertEquals(0, pageResponse.getPage());
        assertEquals(100, pageResponse.getSize());
        assertEquals(50, pageResponse.getTotalElements());
        assertEquals(1, pageResponse.getTotalPages());
        assertEquals(50, pageResponse.getContent().size());
    }
}