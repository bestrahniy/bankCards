package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class AdminControllerTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
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
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtCreatorConfigTest jwtCreatorConfigTest;

    private UsersEntity adminUser;

    private UsersEntity regularUser;

    private UsersEntity anotherUser;

    private RoleEntity adminRole;

    private RoleEntity userRole;

    @BeforeEach
    void setUp() {
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

        anotherUser = createUserWithRoles("anotherUser", "another@example.com", "user456", userRole);
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
    @Transactional
    void grantAdminRole_ShouldReturnOkAndAddAdminRole_WhenUserIsAdmin() throws Exception {
        Set<String> initialRoles = regularUser.getRoles().stream()
                .map(role -> role.getRole().toString())
                .collect(Collectors.toSet());
        assertThat(initialRoles).contains("USER").doesNotContain("ADMIN");

        UUID targetUserId = regularUser.getId();

        mockMvc.perform(post("/api/admin/grant-admin/{userId}", targetUserId)
                        .header("Authorization", "Bearer " + jwtCreatorConfigTest.createToken(adminUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        UsersEntity updatedUser = usersRepository.findById(targetUserId).orElseThrow();
        Set<String> updatedRoles = updatedUser.getRoles().stream()
                .map(role -> role.getRole().toString())
                .collect(Collectors.toSet());
        assertThat(updatedRoles).contains("USER", "ADMIN");
    }

    @Test
    @Transactional
    void grantAdminRole_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        UUID targetUserId = regularUser.getId();
    
        mockMvc.perform(post("/api/admin/grant-admin/{userId}", targetUserId)
                        .header("Authorization", "Bearer " + jwtCreatorConfigTest.createToken(regularUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void grantAdminRole_ShouldNotDuplicateAdminRole_WhenUserAlreadyHasAdminRole() throws Exception {
        mockMvc.perform(post("/api/admin/grant-admin/{userId}", regularUser.getId())
                        .header("Authorization", "Bearer " + jwtCreatorConfigTest.createToken(adminUser)))
                .andExpect(status().isOk());

        UsersEntity userAfterFirstCall = usersRepository.findById(regularUser.getId()).orElseThrow();
        int rolesCountAfterFirstCall = userAfterFirstCall.getRoles().size();

        mockMvc.perform(post("/api/admin/grant-admin/{userId}", regularUser.getId())
                        .header("Authorization", "Bearer " + jwtCreatorConfigTest.createToken(adminUser)))
                .andExpect(status().isOk());

        UsersEntity userAfterSecondCall = usersRepository.findById(regularUser.getId()).orElseThrow();
        assertThat(userAfterSecondCall.getRoles()).hasSize(rolesCountAfterFirstCall);
        
        Set<String> roles = userAfterSecondCall.getRoles().stream()
                .map(role -> role.getRole().toString())
                .collect(Collectors.toSet());
        assertThat(roles).contains("ADMIN");
    }

    @Test
    @Transactional
    void grantAdminRole_ShouldReturnCorrectResponseStructure() throws Exception {
        UUID targetUserId = anotherUser.getId();

        mockMvc.perform(post("/api/admin/grant-admin/{userId}", targetUserId)
                        .header("Authorization", "Bearer " + jwtCreatorConfigTest.createToken(adminUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.login").value("anotherUser"))
                .andExpect(jsonPath("$.email").value("another@example.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").isString())
                .andExpect(jsonPath("$.jwt").doesNotExist());
    }

    @Test
    @Transactional
    void grantAdminRole_ShouldReturnUnauthorized_WhenNoAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/admin/grant-admin/{userId}", regularUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void grantAdminRole_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        mockMvc.perform(post("/api/admin/grant-admin/{userId}", regularUser.getId())
                        .header("Authorization", "Bearer invalid_token_here")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void grantAdminRole_ShouldReturnBadRequest_WhenInvalidUserIdFormat() throws Exception {
        mockMvc.perform(post("/api/admin/grant-admin/{userId}", "not-a-uuid")
                        .header("Authorization", "Bearer " + jwtCreatorConfigTest.createToken(adminUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}