package com.example.bankcards.service;

import org.springframework.stereotype.Service;

import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.mapper.RefreshTokenMapper;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtCreator jwtCreator;

    private final RefreshTokenRepository refreshTokenRepository;

    private final RefreshTokenMapper refreshTokenMapper;

    @Transactional
    public RefreshTokenEntity createRefershToken(UsersEntity user) {
        CreateRefreshTokenRequest createRefreshTokenRequest = jwtCreator.createRefresh(user);

        RefreshTokenEntity refreshTokenEntity = refreshTokenMapper.toEntity(createRefreshTokenRequest, user);

        return refreshTokenRepository.save(refreshTokenEntity);
    }

}
