package com.example.bankcards.integration.service;

import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.UserRegistrationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class UserServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRegistrationServiceImpl userRegistrationService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();
        roleRepository.deleteAll();
        
        RoleEntity userRole = RoleEntity.builder()
                .role(RoleType.USER)
                .build();
        roleRepository.save(userRole);
    }

    @Test
    void registerUser_shouldSaveUserToDatabase() {
        RegistrationRequest request = RegistrationRequest.builder()
                .login("integrationuser")
                .password("integrationpass123")
                .email("integration@example.com")
                .build();

        UserResponse response = userRegistrationService.registerUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getLogin()).isEqualTo("integrationuser");
        assertThat(response.getEmail()).isEqualTo("integration@example.com");
        assertThat(response.getRoles()).isNotEmpty();

        Optional<UsersEntity> savedUser = usersRepository.findById(response.getId());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getLogin()).isEqualTo("integrationuser");
        assertThat(savedUser.get().getEmail()).isEqualTo("integration@example.com");
        assertThat(savedUser.get().isActive()).isTrue();
        assertThat(savedUser.get().getRoles()).isNotEmpty();
        assertThat(passwordEncoder.matches("integrationpass123", savedUser.get().getPassword())).isTrue();
    }

    @Test
    void registerUser_shouldAssignUserRole() {
        RegistrationRequest request = RegistrationRequest.builder()
                .login("roleuser")
                .password("rolepass123")
                .email("role@example.com")
                .build();

        UserResponse response = userRegistrationService.registerUser(request);

        UsersEntity user = usersRepository.findById(response.getId()).orElseThrow();
        assertThat(user.getRoles())
                .extracting(RoleEntity::getRole)
                .containsExactly(RoleType.USER);
    }

    @Test
    void registerUser_shouldHandleMultipleRegistrations() {
        RegistrationRequest user1 = RegistrationRequest.builder()
                .login("user1")
                .password("pass1")
                .email("user1@example.com")
                .build();

        RegistrationRequest user2 = RegistrationRequest.builder()
                .login("user2")
                .password("pass2")
                .email("user2@example.com")
                .build();

        UserResponse response1 = userRegistrationService.registerUser(user1);
        UserResponse response2 = userRegistrationService.registerUser(user2);

        assertThat(response1.getId()).isNotEqualTo(response2.getId());
        assertThat(usersRepository.count()).isEqualTo(2);
    }

    @Test
    void registerUser_shouldEncodePassword() {
        String rawPassword = "MyStrongPassword!123";
        RegistrationRequest request = RegistrationRequest.builder()
                .login("passworduser")
                .password(rawPassword)
                .email("password@example.com")
                .build();

        UserResponse response = userRegistrationService.registerUser(request);

        UsersEntity user = usersRepository.findById(response.getId()).orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, user.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("wrongpassword", user.getPassword())).isFalse();
    }

    @Test
    void registerUser_shouldReturnUserWithRoles() {
        RoleEntity adminRole = RoleEntity.builder()
                .role(RoleType.ADMIN)
                .build();
        roleRepository.save(adminRole);

        RegistrationRequest request = RegistrationRequest.builder()
                .login("multipleroles")
                .password("password")
                .email("multi@example.com")
                .build();

        UserResponse response = userRegistrationService.registerUser(request);

        List<RoleEntity> userRoles = roleRepository.findByRole(RoleType.USER);
        assertThat(response.getRoles()).hasSize(userRoles.size());
    }

    @Test
    void registerUser_shouldSetUserAsActiveByDefault() {
        RegistrationRequest request = RegistrationRequest.builder()
                .login("activeuser")
                .password("password")
                .email("active@example.com")
                .build();

        UserResponse response = userRegistrationService.registerUser(request);

        UsersEntity user = usersRepository.findById(response.getId()).orElseThrow();
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void registerUser_shouldGenerateUniqueIds() {
        RegistrationRequest request1 = RegistrationRequest.builder()
                .login("unique1")
                .password("pass1")
                .email("unique1@example.com")
                .build();

        RegistrationRequest request2 = RegistrationRequest.builder()
                .login("unique2")
                .password("pass2")
                .email("unique2@example.com")
                .build();

        UserResponse response1 = userRegistrationService.registerUser(request1);
        UserResponse response2 = userRegistrationService.registerUser(request2);

        assertThat(response1.getId()).isNotNull();
        assertThat(response2.getId()).isNotNull();
        assertThat(response1.getId()).isNotEqualTo(response2.getId());
    }

}