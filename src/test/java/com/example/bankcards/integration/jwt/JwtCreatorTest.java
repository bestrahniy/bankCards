package com.example.bankcards.integration.jwt;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Container;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.jwt.JwtHelper;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@SpringBootTest
@Testcontainers
public class JwtCreatorTest {

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
    private JwtCreator jwtCreator;

    @Autowired
    private JwtHelper jwtHelper;

    @Test
    void contextLoads() {
        assertNotNull(jwtCreator);
    }

    private UsersEntity createTestUser() {
        UsersEntity user = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .email("test@example.com")
                .password("password")
                .createdAt(Instant.now())
                .isActive(true)
                .build();

        Set<RoleEntity> roles = new HashSet<>();
        RoleEntity role = RoleEntity.builder()
                .id(1L)
                .role(RoleType.USER)
                .isActive(true)
                .build();
        roles.add(role);
        user.setRoles(roles);

        return user;
    }

    @Test
    void createAndValidateJwt_IntegrationTest() {
        UsersEntity user = createTestUser();

        String token = jwtCreator.createJwt(user);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);

        SecretKey key = jwtHelper.generateKey();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(user.getLogin(), claims.get("login", String.class));
        assertEquals("bankCards", claims.getIssuer());
        assertEquals(user.getId().toString(), claims.get("id", String.class));
        assertEquals(user.getEmail(), claims.get("email", String.class));

        var roles = claims.get("role", java.util.List.class);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0));

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

}
