package com.example.bankcards.integration.controller;

import com.example.bankcards.config.JwtCreatorConfigTest;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.EventType;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.NotificationRepository;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class CardCreateNotificationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtCreatorConfigTest jwtCreatorConfigTest;

    private UsersEntity testUser;

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


        testUser = usersRepository.findByLogin("testUser")
                .orElseGet(() -> {
                    UsersEntity user = UsersEntity.builder()
                            .login("testUser")
                            .email("testUser@example.com")
                            .password(passwordEncoder.encode("testUser"))
                            .createdAt(Instant.now())
                            .roles(Set.of(adminRole, userRole))
                            .build();
                    return usersRepository.save(user);
                });
    }

   @Test
    void createCardRequest_WithAdminAndUserRolesInToken_ShouldReturnOk() throws Exception {
        String token = jwtCreatorConfigTest.createToken(testUser);

        mockMvc.perform(post("/api/user/cards/request/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("testUser"))
                .andExpect(jsonPath("$.notification.event").value("CREATE_CARD"));

        assertThat(notificationRepository.count()).isEqualTo(1);
        
        NotificationEntity notification = notificationRepository.findAll().get(0);
        assertThat(notification.getUser().getLogin()).isEqualTo("testUser");
        assertThat(notification.getEvent()).isEqualTo(EventType.CREATE_CARD);
    }

    @Test
    void createCardRequest_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/user/cards/request/create")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        assertThat(notificationRepository.count()).isEqualTo(0);
    }

    @Test
    void createCardRequest_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/user/cards/request/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        assertThat(notificationRepository.count()).isEqualTo(0);
    }


    @Test
    void createCardRequest_MultipleRequests_ShouldCreateMultipleNotifications() throws Exception {
        String token = jwtCreatorConfigTest.createToken(testUser);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/user/cards/request/create")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        assertThat(notificationRepository.count()).isEqualTo(3);
    }

}
