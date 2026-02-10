package com.example.bankcards.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.dto.request.RefreshTokenRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.jwtException.RefreshTokenNotFoundException;
import com.example.bankcards.exception.jwtException.TokenHasExpiredException;
import com.example.bankcards.exception.jwtException.TokenNotActiveException;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.jwt.JwtHelper;
import com.example.bankcards.mapper.RefreshTokenMapperImpl;
import com.example.bankcards.mapper.UserMapperImpl;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtCreator jwtCreator;

    private final RefreshTokenRepository refreshTokenRepository;

    private final RefreshTokenMapperImpl refreshTokenMapper;

    private final JwtHelper jwtHelper;

    private final UserMapperImpl userMapper;

    @Transactional
    public RefreshTokenEntity createRefershToken(UsersEntity user) {
        CreateRefreshTokenRequest createRefreshTokenRequest = jwtCreator.createRefresh(user);

        RefreshTokenEntity refreshTokenEntity = refreshTokenMapper.toEntity(createRefreshTokenRequest, user);

        return refreshTokenRepository.save(refreshTokenEntity);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public UserResponse validateAndRefreshToken(RefreshTokenRequest refreshTokenRequest) {
        String hashToken = refreshTokenRequest.getHashRefreshToken();

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByHashToken(hashToken)
                .orElseThrow(() -> new RefreshTokenNotFoundException(
                    String.format("refresh token: %s has not found", hashToken)
                ));

        UsersEntity user = refreshToken.getUser();
        if (!jwtHelper.validateToken(hashToken, user)) {
            throw new TokenHasExpiredException(String.format("token: %s has expired", hashToken));
        }

        if (!refreshToken.getIsActive()) {
            throw new TokenNotActiveException(String.format("token: %s is not active", hashToken));
        }

        String newAccessToken = jwtCreator.createJwt(user);

        return userMapper.toDtoUserResponse(user, newAccessToken, refreshToken);
    }

}
