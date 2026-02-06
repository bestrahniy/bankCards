package com.example.bankcards.integration.service;

import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class CustomUserDetailsServiceIntegrationTest {

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
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UsersRepository usersRepository;

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();
    }

    @Test
    void loadUserByUsername_shouldReturnActiveUser() {
        UsersEntity activeUser = UsersEntity.builder()
                .login("activeuser")
                .password("password123")
                .email("active@example.com")
                .isActive(true)
                .build();
        
        usersRepository.save(activeUser);

        UserDetails result = userDetailsService.loadUserByUsername("activeuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("activeuser");
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionForInactiveUser() {
        UsersEntity inactiveUser = UsersEntity.builder()
                .login("inactiveuser")
                .password("password123")
                .email("inactive@example.com")
                .isActive(false)
                .build();
        
        usersRepository.save(inactiveUser);

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("inactiveuser");
        });
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionForNonExistentUser() {
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void loadUserByUsername_shouldReturnUserWithCorrectAuthorities() {
        UsersEntity activeUser = UsersEntity.builder()
                .login("authorityuser")
                .password("password123")
                .email("authority@example.com")
                .isActive(true)
                .build();
        
        usersRepository.save(activeUser);

        UserDetails result = userDetailsService.loadUserByUsername("authorityuser");

        assertThat(result.getAuthorities()).isNotNull();
    }

    @Test
    void loadUserByUsername_shouldWorkWithMultipleUsers() {
        UsersEntity user1 = UsersEntity.builder()
                .login("user1")
                .password("pass1")
                .email("user1@example.com")
                .isActive(true)
                .build();
        
        UsersEntity user2 = UsersEntity.builder()
                .login("user2")
                .password("pass2")
                .email("user2@example.com")
                .isActive(true)
                .build();
        
        usersRepository.save(user1);
        usersRepository.save(user2);

        UserDetails result1 = userDetailsService.loadUserByUsername("user1");
        UserDetails result2 = userDetailsService.loadUserByUsername("user2");

        assertThat(result1.getUsername()).isEqualTo("user1");
        assertThat(result2.getUsername()).isEqualTo("user2");
    }

}