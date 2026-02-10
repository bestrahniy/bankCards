package com.example.bankcards.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.mapper.RefreshTokenMapperImpl;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private JwtCreator jwtCreator;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenMapperImpl refreshTokenMapper;
    @InjectMocks
    private RefreshTokenService refreshTokenService;
    private UsersEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .email("test@example.com")
                .password("pass")
                .build();
    }

    @Test
    void createRefreshToken_WithValidUser_ShouldSaveAndReturnRefreshTokenEntity() {
        CreateRefreshTokenRequest request = CreateRefreshTokenRequest.builder()
                .hashToken("test-refresh-token")
                .createdAt(new Date())
                .expiredAt(new Date())
                .build();
        
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .hashToken("test-refresh-token")
                .createdAt(new Date())
                .expireAt(new Date())
                .isActive(true)
                .user(testUser)
                .build();

        when(jwtCreator.createRefresh(testUser)).thenReturn(request);
        when(refreshTokenMapper.toEntity(request, testUser)).thenReturn(refreshTokenEntity);
        when(refreshTokenRepository.save(refreshTokenEntity)).thenReturn(refreshTokenEntity);

        RefreshTokenEntity result = refreshTokenService.createRefershToken(testUser);

        assertNotNull(result);
        assertEquals(refreshTokenEntity, result);
        assertEquals("test-refresh-token", result.getHashToken());
        assertEquals(testUser, result.getUser());
        assertTrue(result.getIsActive());
        verify(jwtCreator, times(1)).createRefresh(testUser);
        verify(refreshTokenMapper, times(1)).toEntity(request, testUser);
        verify(refreshTokenRepository, times(1)).save(refreshTokenEntity);
    }

}