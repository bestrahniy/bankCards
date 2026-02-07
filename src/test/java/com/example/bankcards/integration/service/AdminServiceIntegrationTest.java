package com.example.bankcards.integration.service;

import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.AdminService;
import com.example.bankcards.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Admin;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class AdminServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/db.changelog-master.yaml");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AdminService adminService;

    private UsersEntity admin;

    @BeforeEach
    void setUp() {
        if (!usersRepository.existsByLogin("admin")) {
            RoleEntity roleAdmin = roleRepository.findByRole(RoleType.ADMIN).getFirst();
            RoleEntity roleUser = roleRepository.findByRole(RoleType.USER).getFirst();

            UsersEntity user = UsersEntity.builder()
                .login("admin")
                .password(passwordEncoder.encode("admin"))
                .email("admin@gmail.com")
                .roles(Set.of(roleAdmin, roleUser))
                .build();

            admin = user;
            usersRepository.save(user);
        }

        if (!usersRepository.existsByLogin("testUser")) {
            RoleEntity roleUser = roleRepository.findByRole(RoleType.USER).getFirst();

            UsersEntity user = UsersEntity.builder()
                .login("testUser")
                .password(passwordEncoder.encode("testUser"))
                .email("testUser@gmail.com")
                .roles(Set.of(roleUser))
                .build();

            admin = user;
            usersRepository.save(user);
        }
    }

    @Test
    void whenGrantadmin_WhenUserShouldGetRoleAdmin() {
        UsersEntity user = usersRepository.findByLogin("testUser")
            .orElseThrow(() -> new UserNotFoundException("testUser"));

        UserResponse userResponse = adminService.grantAdminRole(user.getId());

        assertThat(userResponse).isNotNull();
    }

}