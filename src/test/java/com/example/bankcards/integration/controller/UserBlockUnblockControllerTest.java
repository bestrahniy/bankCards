package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
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
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserBlockUnblockControllerTest {

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
    private RoleRepository roleRepository;

    @Autowired
    private JwtCreatorConfigTest jwtCreatorConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsersEntity adminUser;
    private UsersEntity activeRegularUser;
    private UsersEntity inactiveRegularUser;
    private RoleEntity adminRole;
    private RoleEntity userRole;
    private String adminToken;
    private String activeUserToken;

    @BeforeEach
    void setUp() {
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

        adminUser = createUserWithRoles("adminUser", "admin@example.com", "admin123", true, adminRole);
        activeRegularUser = createUserWithRoles("activeUser", "active@example.com", "user123", true, userRole);
        inactiveRegularUser = createUserWithRoles("inactiveUser", "inactive@example.com", "user123", false, userRole);

        adminToken = jwtCreatorConfig.createToken(adminUser);
        activeUserToken = jwtCreatorConfig.createToken(activeRegularUser);
    }

    private UsersEntity createUserWithRoles(String login, String email, String password, boolean isActive, RoleEntity... roles) {
        UsersEntity user = UsersEntity.builder()
                .login(login)
                .email(email)
                .password(passwordEncoder.encode(password))
                .createdAt(Instant.now())
                .isActive(isActive)
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
    void blockUser_Success() throws Exception {
        UUID userId = activeRegularUser.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/block", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("activeUser", response.getLogin());
        assertFalse(response.getIsActive());

        UsersEntity updatedUser = usersRepository.findById(userId).orElseThrow();
        assertFalse(updatedUser.isActive());
    }

    @Test
    void blockUser_AlreadyBlocked_ShouldStillSucceed() throws Exception {
        UUID userId = inactiveRegularUser.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/block", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("inactiveUser", response.getLogin());
        assertFalse(response.getIsActive());

        UsersEntity updatedUser = usersRepository.findById(userId).orElseThrow();
        assertFalse(updatedUser.isActive());
    }

    @Test
    void unblockUser_Success() throws Exception {
        UUID userId = inactiveRegularUser.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/unblock", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("inactiveUser", response.getLogin());
        assertTrue(response.getIsActive());

        UsersEntity updatedUser = usersRepository.findById(userId).orElseThrow();
        assertTrue(updatedUser.isActive());
    }

    @Test
    void unblockUser_AlreadyActive_ShouldStillSucceed() throws Exception {
        UUID userId = activeRegularUser.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/unblock", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("activeUser", response.getLogin());
        assertTrue(response.getIsActive());

        UsersEntity updatedUser = usersRepository.findById(userId).orElseThrow();
        assertTrue(updatedUser.isActive());
    }

    @Test
    void blockUser_UserNotFound_ShouldReturn404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(post("/api/admin/user/{userId}/block", nonExistentUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void unblockUser_UserNotFound_ShouldReturn404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(post("/api/admin/user/{userId}/unblock", nonExistentUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void blockUser_WithoutAdminRole_ShouldReturn403() throws Exception {
        UUID userId = activeRegularUser.getId();

        mockMvc.perform(post("/api/admin/user/{userId}/block", userId)
                        .header("Authorization", "Bearer " + activeUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void unblockUser_WithoutAdminRole_ShouldReturn403() throws Exception {
        UUID userId = inactiveRegularUser.getId();

        mockMvc.perform(post("/api/admin/user/{userId}/unblock", userId)
                        .header("Authorization", "Bearer " + activeUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void blockUser_BlockAdminUser_Success() throws Exception {
        UsersEntity anotherAdmin = createUserWithRoles("anotherAdmin", "admin2@example.com", "admin123", true, adminRole);
        UUID adminUserId = anotherAdmin.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/block", adminUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("anotherAdmin", response.getLogin());
        assertFalse(response.getIsActive());

        UsersEntity updatedAdmin = usersRepository.findById(adminUserId).orElseThrow();
        assertFalse(updatedAdmin.isActive());
    }

    @Test
    void unblockUser_UnblockAdminUser_Success() throws Exception {
        UsersEntity anotherAdmin = createUserWithRoles("anotherAdmin", "admin2@example.com", "admin123", false, adminRole);
        UUID adminUserId = anotherAdmin.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/unblock", adminUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("anotherAdmin", response.getLogin());
        assertTrue(response.getIsActive());

        UsersEntity updatedAdmin = usersRepository.findById(adminUserId).orElseThrow();
        assertTrue(updatedAdmin.isActive());
    }

    @Test
    void blockUser_UserWithMultipleRoles_Success() throws Exception {
        UsersEntity multiRoleUser = createUserWithRoles("multiRoleUser", "multi@example.com", "pass123", true, adminRole, userRole);
        UUID multiRoleUserId = multiRoleUser.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/block", multiRoleUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("multiRoleUser", response.getLogin());
        assertFalse(response.getIsActive());

        UsersEntity updatedUser = usersRepository.findById(multiRoleUserId).orElseThrow();
        assertFalse(updatedUser.isActive());
    }

    @Test
    void blockUnblockCycle_Success() throws Exception {
        UUID userId = activeRegularUser.getId();

        String blockResponseJson = mockMvc.perform(post("/api/admin/user/{userId}/block", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse blockResponse = objectMapper.readValue(blockResponseJson, UserActiveResponse.class);
        assertFalse(blockResponse.getIsActive());

        UsersEntity afterBlock = usersRepository.findById(userId).orElseThrow();
        assertFalse(afterBlock.isActive());

        String unblockResponseJson = mockMvc.perform(post("/api/admin/user/{userId}/unblock", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse unblockResponse = objectMapper.readValue(unblockResponseJson, UserActiveResponse.class);
        assertTrue(unblockResponse.getIsActive());

        UsersEntity afterUnblock = usersRepository.findById(userId).orElseThrow();
        assertTrue(afterUnblock.isActive());
    }

    @Test
    void blockUser_BlockSelf_ShouldSucceed() throws Exception {
        UUID adminUserId = adminUser.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/block", adminUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("adminUser", response.getLogin());
        assertFalse(response.getIsActive());

        UsersEntity updatedAdmin = usersRepository.findById(adminUserId).orElseThrow();
        assertFalse(updatedAdmin.isActive());
    }

    @Test
    void blockUser_NoToken_ShouldReturn401() throws Exception {
        UUID userId = activeRegularUser.getId();

        mockMvc.perform(post("/api/admin/user/{userId}/block", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unblockUser_NoToken_ShouldReturn401() throws Exception {
        UUID userId = inactiveRegularUser.getId();

        mockMvc.perform(post("/api/admin/user/{userId}/unblock", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blockUser_InvalidToken_ShouldReturn401() throws Exception {
        UUID userId = activeRegularUser.getId();

        mockMvc.perform(post("/api/admin/user/{userId}/block", userId)
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blockUser_UserWithCards_Success() throws Exception {
        UsersEntity userWithCards = createUserWithRoles("userWithCards", "cards@example.com", "pass123", true, userRole);
        UUID userId = userWithCards.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/block", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("userWithCards", response.getLogin());
        assertFalse(response.getIsActive());

        UsersEntity updatedUser = usersRepository.findById(userId).orElseThrow();
        assertFalse(updatedUser.isActive());
    }

    @Test
    void multipleBlockOperations_Success() throws Exception {
        UsersEntity user1 = createUserWithRoles("user1", "user1@example.com", "pass123", true, userRole);
        UsersEntity user2 = createUserWithRoles("user2", "user2@example.com", "pass123", true, userRole);
        UsersEntity user3 = createUserWithRoles("user3", "user3@example.com", "pass123", true, userRole);

        mockMvc.perform(post("/api/admin/user/{userId}/block", user1.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/user/{userId}/block", user2.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/user/{userId}/unblock", user3.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        UsersEntity updatedUser1 = usersRepository.findById(user1.getId()).orElseThrow();
        UsersEntity updatedUser2 = usersRepository.findById(user2.getId()).orElseThrow();
        UsersEntity updatedUser3 = usersRepository.findById(user3.getId()).orElseThrow();

        assertFalse(updatedUser1.isActive());
        assertFalse(updatedUser2.isActive());
        assertTrue(updatedUser3.isActive());
    }

    @Test
    void blockUser_ResponseFormat_Correct() throws Exception {
        UUID userId = activeRegularUser.getId();

        String responseJson = mockMvc.perform(post("/api/admin/user/{userId}/block", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserActiveResponse response = objectMapper.readValue(responseJson, UserActiveResponse.class);
        
        assertNotNull(response);
        assertEquals("activeUser", response.getLogin());
        assertFalse(response.getIsActive());
        
        assertTrue(responseJson.contains("\"login\""));
        assertTrue(responseJson.contains("\"isActive\""));
    }
}