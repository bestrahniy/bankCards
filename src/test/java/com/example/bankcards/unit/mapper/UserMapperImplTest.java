package com.example.bankcards.unit.mapper;

import com.example.bankcards.mapper.UserMapperImpl;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperImplTest {

    private UserMapperImpl userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
    }

    @Test
    void toEntity_WithValidRegistrationRequest_ShouldMapCorrectly() {
        RegistrationRequest request = RegistrationRequest.builder()
                .login("testuser")
                .email("test@example.com")
                .password("SecurePass123!")
                .build();

        UsersEntity result = userMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.isActive());
        assertNull(result.getPassword());
        assertNull(result.getId());
    }

    @Test
    void toEntity_WithNullRegistrationRequest_ShouldReturnNull() {
        UsersEntity result = userMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toDtoUserResponse_WithValidUserEntity_ShouldMapCorrectly() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(RoleEntity.builder()
                .id(1L)
                .role(RoleType.USER)
                .isActive(true)
                .build());
        roles.add(RoleEntity.builder()
                .id(2L)
                .role(RoleType.ADMIN)
                .isActive(true)
                .build());

        UsersEntity user = UsersEntity.builder()
                .id(userId)
                .login("testuser")
                .email("test@example.com")
                .createdAt(createdAt)
                .isActive(true)
                .roles(roles)
                .build();

        UserResponse result = userMapper.toDtoUserResponse(user);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getLogin());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(createdAt, result.getCreatedAt());
        
        assertNotNull(result.getRoles());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains("USER"));
        assertTrue(result.getRoles().contains("ADMIN"));
        
        assertNull(result.getJwt());
        assertNull(result.getRefreshToken());
    }

    @Test
    void toDtoUserResponse_WithTokens_ShouldIncludeTokens() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now().minus(1, ChronoUnit.DAYS);
        
        Set<RoleEntity> roles = new HashSet<>();
        RoleEntity userRole = RoleEntity.builder()
                .id(1L)
                .role(RoleType.USER)
                .isActive(true)
                .build();
        roles.add(userRole);

        UsersEntity user = UsersEntity.builder()
                .id(userId)
                .login("testuser")
                .email("test@example.com")
                .createdAt(createdAt)
                .isActive(true)
                .roles(roles)
                .build();

        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String refreshTokenHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .hashToken(refreshTokenHash)
                .createdAt(new Date())
                .expireAt(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .isActive(true)
                .user(user)
                .build();

        UserResponse result = userMapper.toDtoUserResponse(user, jwtToken, refreshToken);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getLogin());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(createdAt, result.getCreatedAt());
        
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains("USER"));
        
        assertEquals(jwtToken, result.getJwt());
        assertEquals(refreshTokenHash, result.getRefreshToken());
    }

    @Test
    void toDtoUserResponse_WithNullUserEntity_ShouldReturnNull() {
        UserResponse result = userMapper.toDtoUserResponse(null);
        assertNull(result);
    }

    @Test
    void toDtoUserResponse_WithNullUserEntityAndTokens_ShouldReturnNull() {
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .hashToken("some_hash")
                .build();

        UserResponse result = userMapper.toDtoUserResponse(null, jwtToken, refreshToken);
        assertNull(result);
    }

    @Test
    void toDtoUserActiveResponse_WithActiveUser_ShouldMapCorrectly() {
        UsersEntity user = UsersEntity.builder()
                .login("activeuser")
                .isActive(true)
                .build();

        UserActiveResponse result = userMapper.toDtoUserActiveResponse(user);

        assertNotNull(result);
        assertEquals("activeuser", result.getLogin());
        assertTrue(result.getIsActive());
    }

    @Test
    void toDtoUserActiveResponse_WithInactiveUser_ShouldMapCorrectly() {
        UsersEntity user = UsersEntity.builder()
                .login("inactiveuser")
                .isActive(false)
                .build();

        UserActiveResponse result = userMapper.toDtoUserActiveResponse(user);

        assertNotNull(result);
        assertEquals("inactiveuser", result.getLogin());
        assertFalse(result.getIsActive());
    }

    @Test
    void toDtoUserActiveResponse_WithNullLogin_ShouldMapCorrectly() {
        UsersEntity user = UsersEntity.builder()
                .login(null)
                .isActive(true)
                .build();

        UserActiveResponse result = userMapper.toDtoUserActiveResponse(user);

        assertNotNull(result);
        assertNull(result.getLogin());
        assertTrue(result.getIsActive());
    }

    @Test
    void toDtoUserResponse_WithEmptyRoles_ShouldMapEmptySet() {
        UsersEntity user = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .email("test@example.com")
                .createdAt(Instant.now())
                .isActive(true)
                .roles(new HashSet<>())
                .build();

        UserResponse result = userMapper.toDtoUserResponse(user);

        assertNotNull(result);
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
    }

    @Test
    void toDtoUserResponse_WithNullRoles_ShouldHandleGracefully() {
        UsersEntity user = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .email("test@example.com")
                .createdAt(Instant.now())
                .isActive(true)
                .roles(null)
                .build();

        assertThrows(NullPointerException.class, () -> {
            userMapper.toDtoUserResponse(user);
        });
    }

    @Test
    void toDtoUserResponse_ShouldConvertRolesToStrings() {
        RoleEntity userRole = RoleEntity.builder()
                .id(1L)
                .role(RoleType.USER)
                .isActive(true)
                .build();
        
        RoleEntity adminRole = RoleEntity.builder()
                .id(2L)
                .role(RoleType.ADMIN)
                .isActive(true)
                .build();

        UsersEntity user = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .email("test@example.com")
                .createdAt(Instant.now())
                .isActive(true)
                .roles(Set.of(userRole, adminRole))
                .build();

        UserResponse result = userMapper.toDtoUserResponse(user);

        assertNotNull(result.getRoles());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains("USER"));
        assertTrue(result.getRoles().contains("ADMIN"));
    }

    @Test
    void toEntity_WithMinimumValidData_ShouldMapCorrectly() {
        RegistrationRequest request = RegistrationRequest.builder()
                .login("user")
                .email("a@b.c")
                .password("pass")
                .build();

        UsersEntity result = userMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("user", result.getLogin());
        assertEquals("a@b.c", result.getEmail());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void toEntity_ShouldSetCurrentTimestamp() {
        RegistrationRequest request = RegistrationRequest.builder()
                .login("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        Instant beforeMapping = Instant.now().minus(1, ChronoUnit.SECONDS);

        UsersEntity result = userMapper.toEntity(request);

        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isAfter(beforeMapping) || 
                  result.getCreatedAt().equals(beforeMapping));
        assertTrue(result.getCreatedAt().isBefore(Instant.now().plus(1, ChronoUnit.SECONDS)));
    }
}