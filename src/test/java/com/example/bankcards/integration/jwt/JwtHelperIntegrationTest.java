package com.example.bankcards.integration.jwt;

import com.example.bankcards.jwt.JwtHelper;
import com.example.bankcards.model.entity.UsersEntity;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class JwtHelperIntegrationTest {

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
    private JwtHelper jwtHelper;

    @Test
    void generateKey_shouldCreateConsistentKey() {
        var key1 = jwtHelper.generateKey();
        var key2 = jwtHelper.generateKey();

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void validateToken_shouldWorkWithGeneratedToken() {
        String token = Jwts.builder()
                .claim("login", "testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(jwtHelper.generateKey())
                .compact();

        UserDetails userDetails = new UsersEntity();
        ((UsersEntity) userDetails).setLogin("testuser");

        boolean isValid = jwtHelper.validateToken(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void extractLogin_shouldExtractFromValidToken() {
        String token = Jwts.builder()
                .claim("login", "integrationuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(jwtHelper.generateKey())
                .compact();

        String login = jwtHelper.extractLogin(token);

        assertThat(login).isEqualTo("integrationuser");
    }

    @Test
    void extractExpiration_shouldReturnFutureDate() {
        Date futureDate = new Date(System.currentTimeMillis() + 10000);
        String token = Jwts.builder()
                .claim("login", "testuser")
                .setIssuedAt(new Date())
                .setExpiration(futureDate)
                .signWith(jwtHelper.generateKey())
                .compact();

        Date expiration = jwtHelper.extractExpiration(token);

        assertThat(expiration).isCloseTo(futureDate, 1000);
    }

}