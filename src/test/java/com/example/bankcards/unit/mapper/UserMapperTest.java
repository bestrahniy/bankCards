package com.example.bankcards.unit.mapper;

import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toEntity_shouldCorrectlyConvertRegistrationRequestToUsersEntity() {
        RegistrationRequest request = RegistrationRequest.builder()
                .login("testUser")
                .email("test@example.com")
                .password("password123")
                .build();

        UsersEntity result = userMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("testUser", result.getLogin());
        assertEquals("test@example.com", result.getEmail());
        assertNull(result.getId());
        assertNull(result.getPassword());
        assertNull(result.getCreatedAt());
        assertNull(result.getRoles());
        assertNull(result.getBankCardsEntities());
        assertNull(result.getRefreshTokens());
        assertTrue(result.isActive());
    }

    @Test
    void toEntity_shouldHandleNullRegistrationRequest() {
        UsersEntity result = userMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toDto_shouldCorrectlyConvertUsersEntityToUserResponse() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Set<RoleEntity> roles = Set.of(
                RoleEntity.builder().id(1L).build(),
                RoleEntity.builder().id(2L).build()
        );

        UsersEntity entity = UsersEntity.builder()
                .id(userId)
                .login("testUser")
                .email("test@example.com")
                .createdAt(createdAt)
                .roles(roles)
                .password("hashedPassword")
                .isActive(true)
                .bankCardsEntities(null)
                .refreshTokens(null)
                .build();

        UserResponse result = userMapper.toDto(entity);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testUser", result.getLogin());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(createdAt, result.getCraetedAt());
        assertEquals(roles, result.getRoles());
    }

    @Test
    void toDto_shouldCorrectlyConvertUsersEntityWithoutRoles() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        UsersEntity entity = UsersEntity.builder()
                .id(userId)
                .login("testUser")
                .email("test@example.com")
                .createdAt(createdAt)
                .roles(null)
                .password("hashedPassword")
                .build();

        UserResponse result = userMapper.toDto(entity);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testUser", result.getLogin());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(createdAt, result.getCraetedAt());
        assertNull(result.getRoles());
    }

    @Test
    void toDto_shouldCorrectlyConvertUsersEntityWithoutCreatedAt() {
        UUID userId = UUID.randomUUID();

        UsersEntity entity = UsersEntity.builder()
                .id(userId)
                .login("testUser")
                .email("test@example.com")
                .createdAt(null)
                .roles(Set.of())
                .build();

        UserResponse result = userMapper.toDto(entity);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testUser", result.getLogin());
        assertEquals("test@example.com", result.getEmail());
        assertNull(result.getCraetedAt());
        assertThat(result.getRoles()).isEmpty();
    }

    @Test
    void toDto_shouldHandleNullUsersEntity() {
        UserResponse result = userMapper.toDto(null);
        assertNull(result);
    }

    @Test
    void toDto_shouldHandleUsersEntityWithNullFields() {
        UsersEntity entity = UsersEntity.builder()
                .id(null)
                .login(null)
                .email(null)
                .createdAt(null)
                .roles(null)
                .build();

        UserResponse result = userMapper.toDto(entity);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getLogin());
        assertNull(result.getEmail());
        assertNull(result.getCraetedAt());
        assertNull(result.getRoles());
    }

    @Test
    void roundTripConversion_shouldPreserveAllData() {
        RegistrationRequest request = RegistrationRequest.builder()
                .login("testUser")
                .email("test@example.com")
                .password("password123")
                .build();

        UsersEntity entity = userMapper.toEntity(request);
        
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Set<RoleEntity> roles = Set.of(RoleEntity.builder().id(1L).build());
        
        entity.setId(userId);
        entity.setCreatedAt(createdAt);
        entity.setRoles(roles);
        entity.setPassword("hashedPassword");
        entity.setActive(true);

        UserResponse result = userMapper.toDto(entity);

        assertEquals(userId, result.getId());
        assertEquals("testUser", result.getLogin());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(createdAt, result.getCraetedAt());
        assertEquals(roles, result.getRoles());
    }

    @Test
    void toDto_shouldHaveCorrectFieldTypes() {
        UsersEntity entity = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("user")
                .email("email@test.com")
                .createdAt(Instant.now())
                .roles(Set.of())
                .build();

        UserResponse result = userMapper.toDto(entity);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", entity.getId())
                .hasFieldOrPropertyWithValue("login", entity.getLogin())
                .hasFieldOrPropertyWithValue("email", entity.getEmail())
                .hasFieldOrPropertyWithValue("craetedAt", entity.getCreatedAt())
                .hasFieldOrPropertyWithValue("roles", entity.getRoles());
    }

}