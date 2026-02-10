package com.example.bankcards.integration.mapper;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.mapper.RefreshTokenMapperImpl;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;

class RefreshTokenMapperTest {

    private RefreshTokenMapperImpl refreshTokenMapper;

    @BeforeEach
    void setUp() {
        refreshTokenMapper = new RefreshTokenMapperImpl();
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
    }

}