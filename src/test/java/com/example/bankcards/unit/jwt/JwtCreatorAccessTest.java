package com.example.bankcards.unit.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.bankcards.exception.requestException.RolesEmptyException;
import com.example.bankcards.exception.userException.UserNullException;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.jwt.JwtHelper;
import com.example.bankcards.mapper.RefreshTokenMapper;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtCreatorAccessTest {

    private JwtCreator jwtCreator;

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    private SecretKey secretKey;

    private UsersEntity testUser;

    @BeforeEach
    void setUp() {
        jwtCreator = new JwtCreator(jwtHelper, refreshTokenMapper);
        ReflectionTestUtils.setField(jwtCreator, "accessTokenExpiration", "300");
        ReflectionTestUtils.setField(jwtCreator, "refreshTokenExpiration", "900");

        byte[] keyBytes = "9f3c8b2a1d4e7f6a5c8d0e2b9a4f6c7d1e8b3a5f0c9d2e6a7b4f8c1d5e".getBytes();
        secretKey = Keys.hmacShaKeyFor(keyBytes);
        
        testUser = UsersEntity.builder()
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
        testUser.setRoles(roles);
    }
    
    @Test
    void createJwt_WithValidUser_ReturnsToken() {
        when(jwtHelper.generateKey()).thenReturn(secretKey);
        
        String token = jwtCreator.createJwt(testUser);
        
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        assertEquals(testUser.getLogin(), claims.get("login"));
        assertEquals("bankCards", claims.getIssuer());
        assertEquals(testUser.getId().toString(), claims.get("id"));
        assertEquals(testUser.getEmail(), claims.get("email"));
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }
    
    @Test
    void createJwt_WithNullUser_ThrowsException() {
        assertThrows(UserNullException.class, () -> {
            jwtCreator.createJwt(null);
        });
    }
    
    @Test
    void createJwt_WithEmptyRoles_ThrowsException() {
        testUser.setRoles(new HashSet<>());
        
        assertThrows(RolesEmptyException.class, () -> {
            jwtCreator.createJwt(testUser);
        });
    }
    
    @Test
    void createJwt_WithMultipleRoles_IncludesAllRoles() {
        when(jwtHelper.generateKey()).thenReturn(secretKey);
        
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(RoleEntity.builder().id(1L).role(RoleType.USER).build());
        roles.add(RoleEntity.builder().id(2L).role(RoleType.ADMIN).build());
        testUser.setRoles(roles);
        
        String token = jwtCreator.createJwt(testUser);
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        var rolesList = claims.get("role", java.util.List.class);
        assertNotNull(rolesList);
        assertEquals(2, rolesList.size());
        assertTrue(rolesList.contains("USER"));
        assertTrue(rolesList.contains("ADMIN"));
    }
    
    @Test
    void token_HasCorrectExpirationTime() {
        when(jwtHelper.generateKey()).thenReturn(secretKey);
        
        String token = jwtCreator.createJwt(testUser);
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();
        long diffInSeconds = (expiration.getTime() - issuedAt.getTime()) / 1000;
        
        assertEquals(300, diffInSeconds);
    }

}