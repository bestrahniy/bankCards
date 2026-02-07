package com.example.bankcards.unit.mapper;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.mapper.RefreshTokenMapper;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;

class RefreshTokenMapperTest {

    private RefreshTokenMapper refreshTokenMapper;

    @BeforeEach
    void setUp() {
        refreshTokenMapper = new RefreshTokenMapper();
    }

    @Test
    void toDto_WithValidData_ShouldReturnCreateRefreshTokenRequest() {
        String refreshToken = "test-refresh-token";
        Date createdAt = new Date();
        Date expiredAt = new Date(System.currentTimeMillis() + 86400000L);

        CreateRefreshTokenRequest result = refreshTokenMapper.toDto(refreshToken, createdAt, expiredAt);

        assertNotNull(result);
        assertEquals(refreshToken, result.getHashToken());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(expiredAt, result.getExpiredAt());
    }

    @Test
    void toEntity_WithValidData_ShouldReturnRefreshTokenEntity() {
        UsersEntity user = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .email("test@example.com")
                .password("pass")
                .build();
        
        CreateRefreshTokenRequest request = CreateRefreshTokenRequest.builder()
                .hashToken("refresh-token")
                .createdAt(new Date())
                .expiredAt(new Date())
                .build();

        RefreshTokenEntity result = refreshTokenMapper.toEntity(request, user);

        assertNotNull(result);
        assertEquals(request.getHashToken(), result.getHashToken());
        assertEquals(request.getCreatedAt(), result.getCreatedAt());
        assertEquals(request.getExpiredAt(), result.getExpireAt());
        assertEquals(user, result.getUser());
        assertTrue(result.getIsActive());
    }

    @Test
    void toEntity_WithNullUser_ShouldThrowException() {
        CreateRefreshTokenRequest request = CreateRefreshTokenRequest.builder()
                .hashToken("token")
                .build();

        assertThrows(NullPointerException.class, () -> refreshTokenMapper.toEntity(request, null));
    }
}