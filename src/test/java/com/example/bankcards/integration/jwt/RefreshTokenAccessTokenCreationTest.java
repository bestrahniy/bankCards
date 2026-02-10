package com.example.bankcards.integration.jwt;

import com.example.bankcards.dto.request.RefreshTokenRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.jwt.JwtHelper;
import com.example.bankcards.model.entity.*;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RefreshTokenAccessTokenCreationTest {

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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("jwt.secret", () -> "test-jwt-secret-key-for-testing-purposes-only-very-long-key");
        registry.add("jwt.expiration", () -> "86400000");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtCreator jwtCreator;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsersEntity user;
    private RoleEntity userRole;
    private RoleEntity adminRole;
    private String validRefreshTokenHash;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
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

        user = createUserWithRoles("testuser", "test@example.com", "password123", userRole);
        validRefreshTokenHash = "valid-refresh-token-hash-" + UUID.randomUUID();
        createRefreshToken(user, validRefreshTokenHash, true);
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

    private RefreshTokenEntity createRefreshToken(UsersEntity user, String hashToken, boolean isActive) {
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .hashToken(hashToken)
                .user(user)
                .isActive(isActive)
                .createdAt(Date.from(Instant.now()))
                .expireAt(Date.from(Instant.now().plusSeconds(86400)))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Test
    void createAccessToken_Success() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(validRefreshTokenHash)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        
        assertNotNull(response);
        assertNotNull(response.getJwt());
        assertThat(response.getJwt()).isNotEmpty();
        
        String newAccessToken = response.getJwt();
        
        assertThat(newAccessToken).isNotEqualTo(validRefreshTokenHash);
        String[] tokenParts = newAccessToken.split("\\.");
        assertThat(tokenParts.length).isEqualTo(3);
        
        String loginFromToken = jwtHelper.extractLogin(newAccessToken);
        assertThat(loginFromToken).isEqualTo(user.getLogin());
        
        Date expiration = jwtHelper.extractExpiration(newAccessToken);
        assertThat(expiration).isAfter(new Date());
        
        boolean isValid = jwtHelper.validateToken(newAccessToken, user);
        assertThat(isValid).isTrue();
        
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getLogin(), response.getLogin());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(validRefreshTokenHash, response.getRefreshToken());
        assertEquals(1, response.getRoles().size());
        assertTrue(response.getRoles().contains("USER"));
    }

    @Test
    void createAccessToken_WithUserDetails_Success() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(validRefreshTokenHash)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        String newAccessToken = response.getJwt();
        
        UsersEntity userFromToken = new UsersEntity();
        userFromToken.setLogin(jwtHelper.extractLogin(newAccessToken));
        
        boolean isValidForUser = jwtHelper.validateToken(newAccessToken, userFromToken);
        assertThat(isValidForUser).isTrue();
    }

    @Test
    void createAccessToken_ContainsUserClaims_Success() throws Exception {
        UsersEntity userWithAllFields = UsersEntity.builder()
                .login("fulluser")
                .email("full@example.com")
                .password(passwordEncoder.encode("pass123"))
                .createdAt(Instant.now())
                .isActive(true)
                .build();
        
        userWithAllFields.setRoles(new HashSet<>());
        userWithAllFields.getRoles().add(adminRole);
        userWithAllFields.getRoles().add(userRole);
        userWithAllFields = usersRepository.save(userWithAllFields);

        String userTokenHash = "user-token-hash-" + UUID.randomUUID();
        createRefreshToken(userWithAllFields, userTokenHash, true);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(userTokenHash)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        String newAccessToken = response.getJwt();
        
        String loginFromToken = jwtHelper.extractLogin(newAccessToken);
        assertThat(loginFromToken).isEqualTo("fulluser");
        
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRoles().contains("USER"));
        assertTrue(response.getRoles().contains("ADMIN"));
    }

    @Test
    void createAccessToken_MultipleUsers_DifferentTokens() throws Exception {
        UsersEntity user1 = createUserWithRoles("user1", "user1@example.com", "pass123", userRole);
        UsersEntity user2 = createUserWithRoles("user2", "user2@example.com", "pass123", userRole);
        
        String tokenHash1 = "token-hash-1-" + UUID.randomUUID();
        String tokenHash2 = "token-hash-2-" + UUID.randomUUID();
        
        createRefreshToken(user1, tokenHash1, true);
        createRefreshToken(user2, tokenHash2, true);

        RefreshTokenRequest request1 = RefreshTokenRequest.builder()
                .hashRefreshToken(tokenHash1)
                .build();

        String responseJson1 = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RefreshTokenRequest request2 = RefreshTokenRequest.builder()
                .hashRefreshToken(tokenHash2)
                .build();

        String responseJson2 = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response1 = objectMapper.readValue(responseJson1, UserResponse.class);
        UserResponse response2 = objectMapper.readValue(responseJson2, UserResponse.class);
        
        String token1 = response1.getJwt();
        String token2 = response2.getJwt();
        
        assertThat(token1).isNotEqualTo(token2);
        
        String login1 = jwtHelper.extractLogin(token1);
        String login2 = jwtHelper.extractLogin(token2);
        
        assertThat(login1).isEqualTo("user1");
        assertThat(login2).isEqualTo("user2");
    }

    @Test
    void createAccessToken_SameUserSameRefreshToken_SameClaims() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(validRefreshTokenHash)
                .build();

        String responseJson1 = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String responseJson2 = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response1 = objectMapper.readValue(responseJson1, UserResponse.class);
        UserResponse response2 = objectMapper.readValue(responseJson2, UserResponse.class);
        
        String token1 = response1.getJwt();
        String token2 = response2.getJwt();
        
        assertThat(token1).isNotEqualTo(token2);
        
        String login1 = jwtHelper.extractLogin(token1);
        String login2 = jwtHelper.extractLogin(token2);
        
        assertThat(login1).isEqualTo(login2).isEqualTo(user.getLogin());
        
        Date exp1 = jwtHelper.extractExpiration(token1);
        Date exp2 = jwtHelper.extractExpiration(token2);
        
        long diff = Math.abs(exp1.getTime() - exp2.getTime());
        assertThat(diff).isLessThan(10000L);
    }

    @Test
    void createAccessToken_ValidExpirationTime() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(validRefreshTokenHash)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        String newAccessToken = response.getJwt();
        
        Date expiration = jwtHelper.extractExpiration(newAccessToken);
        Date now = new Date();
        Date future = new Date(now.getTime() + 86400000 + 10000);
        
        assertThat(expiration).isAfter(now);
        assertThat(expiration).isBefore(future);
    }

    @Test
    void createAccessToken_InvalidToken_ShouldNotCreate() throws Exception {
        String invalidTokenHash = "invalid-token-hash";
        
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(invalidTokenHash)
                .build();

        mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAccessToken_JwtCreatorIntegration() throws Exception {
        String manualJwtToken = jwtCreator.createJwt(user);
        
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(validRefreshTokenHash)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        String generatedToken = response.getJwt();
        
        assertThat(generatedToken).isNotEqualTo(manualJwtToken);
        
        String manualLogin = jwtHelper.extractLogin(manualJwtToken);
        String generatedLogin = jwtHelper.extractLogin(generatedToken);
        
        assertThat(manualLogin).isEqualTo(generatedLogin).isEqualTo(user.getLogin());
        
        boolean manualValid = jwtHelper.validateToken(manualJwtToken, user);
        boolean generatedValid = jwtHelper.validateToken(generatedToken, user);
        
        assertThat(manualValid).isTrue();
        assertThat(generatedValid).isTrue();
    }

    @Test
    void createAccessToken_WithExpiredRefreshToken_ShouldNotCreate() throws Exception {
        String expiredTokenHash = "expired-token-hash-" + UUID.randomUUID();
        RefreshTokenEntity expiredToken = RefreshTokenEntity.builder()
                .hashToken(expiredTokenHash)
                .user(user)
                .isActive(true)
                .createdAt(Date.from(Instant.now()))
                .expireAt(Date.from(Instant.now().plusSeconds(86400)))
                .build();
        refreshTokenRepository.save(expiredToken);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(expiredTokenHash)
                .build();

        mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("token: " + expiredTokenHash + " has expired"));
    }

    @Test
    void createAccessToken_WithInactiveRefreshToken_ShouldNotCreate() throws Exception {
        String inactiveTokenHash = "inactive-token-hash-" + UUID.randomUUID();
        createRefreshToken(user, inactiveTokenHash, false);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(inactiveTokenHash)
                .build();

        mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("token: " + inactiveTokenHash + " is not active"));
    }

    @Test
    void createAccessToken_UserWithoutRoles_Success() throws Exception {
        UsersEntity userWithoutRoles = UsersEntity.builder()
                .login("noroles")
                .email("noroles@example.com")
                .password(passwordEncoder.encode("pass123"))
                .createdAt(Instant.now())
                .isActive(true)
                .build();

        userWithoutRoles = usersRepository.save(userWithoutRoles);

        String noRolesTokenHash = "no-roles-token-hash-" + UUID.randomUUID();
        createRefreshToken(userWithoutRoles, noRolesTokenHash, true);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(noRolesTokenHash)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        
        assertNotNull(response);
        assertEquals(userWithoutRoles.getId(), response.getId());
        assertEquals(userWithoutRoles.getLogin(), response.getLogin());
        assertTrue(response.getRoles().isEmpty());
        
        String newAccessToken = response.getJwt();
        String loginFromToken = jwtHelper.extractLogin(newAccessToken);
        assertThat(loginFromToken).isEqualTo("noroles");
    }

    @Test
    void createAccessToken_EmptyToken_ShouldReturn400() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken("")
                .build();

        mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccessToken_NullToken_ShouldReturn400() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(null)
                .build();

        mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccessToken_InactiveUser_Success() throws Exception {
        UsersEntity inactiveUser = createUserWithRoles("inactive", "inactive@example.com", "pass123", userRole);
        inactiveUser.setActive(false);
        usersRepository.save(inactiveUser);

        String inactiveUserTokenHash = "inactive-user-token-hash-" + UUID.randomUUID();
        createRefreshToken(inactiveUser, inactiveUserTokenHash, true);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .hashRefreshToken(inactiveUserTokenHash)
                .build();

        String responseJson = mockMvc.perform(post("/api/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse response = objectMapper.readValue(responseJson, UserResponse.class);
        
        assertNotNull(response);
        assertEquals(inactiveUser.getId(), response.getId());
        assertEquals(inactiveUser.getLogin(), response.getLogin());
        assertFalse(inactiveUser.isActive());
    }
}