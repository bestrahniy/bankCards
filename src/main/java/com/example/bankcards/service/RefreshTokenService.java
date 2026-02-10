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
import lombok.extern.slf4j.Slf4j;

/**
 * Service for refresh token management and validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final JwtCreator jwtCreator;

    private final RefreshTokenRepository refreshTokenRepository;

    private final RefreshTokenMapperImpl refreshTokenMapper;

    private final JwtHelper jwtHelper;

    private final UserMapperImpl userMapper;

    /**
     * Creates a new refresh token for a user.
     * 
     * Generates refresh token with expiration date and associates it
     * with the user for future authentication token refresh requests.
     * 
     * @param user user entity to create token for
     * @return created refresh token entity
     */
    @Transactional
    public RefreshTokenEntity createRefershToken(UsersEntity user) {
        log.info("Creating refresh token for user: {}", user.getLogin());
        
        CreateRefreshTokenRequest createRefreshTokenRequest = jwtCreator.createRefresh(user);
        log.trace("Refresh token request created for user ID: {}", user.getId());

        RefreshTokenEntity refreshTokenEntity = refreshTokenMapper.toEntity(createRefreshTokenRequest, user);
        log.debug("Refresh token entity mapped, expires at: {}", refreshTokenEntity.getExpireAt());

        RefreshTokenEntity savedToken = refreshTokenRepository.save(refreshTokenEntity);
        log.info("Refresh token created successfully with ID: {}", savedToken.getId());
        
        return savedToken;
    }

    /**
     * Validates a refresh token and issues new access token.
     * 
     * Checks token existence, validity, expiration, and active status.
     * If validation passes, creates new JWT access token for the user.
     * 
     * @param refreshTokenRequest contains refresh token hash
     * @return user response with new access token
     * @throws RefreshTokenNotFoundException if token doesn't exist
     * @throws TokenHasExpiredException if token is expired
     * @throws TokenNotActiveException if token is inactive
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserResponse validateAndRefreshToken(RefreshTokenRequest refreshTokenRequest) {
        String hashToken = refreshTokenRequest.getHashRefreshToken();
        log.info("Validating and refreshing token (hash: {}...)", 
            hashToken != null && hashToken.length() > 10 ? hashToken.substring(0, 10) + "..." : "null");
        
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByHashToken(hashToken)
                .orElseThrow(() -> {
                    log.error("Refresh token not found in repository");
                    return new RefreshTokenNotFoundException(
                        String.format("refresh token: %s has not found", hashToken)
                    );
                });
        log.debug("Refresh token found with ID: {}, user: {}", 
            refreshToken.getId(), refreshToken.getUser().getLogin());

        UsersEntity user = refreshToken.getUser();
        
        if (!jwtHelper.validateToken(hashToken, user)) {
            log.error("Refresh token validation failed for user: {}", user.getLogin());
            throw new TokenHasExpiredException(String.format("token: %s has expired", hashToken));
        }
        log.debug("Token validation passed");

        if (!refreshToken.getIsActive()) {
            log.error("Refresh token is inactive for user: {}", user.getLogin());
            throw new TokenNotActiveException(String.format("token: %s is not active", hashToken));
        }
        log.debug("Token active status check passed");

        String newAccessToken = jwtCreator.createJwt(user);
        log.info("New access token generated for user: {}", user.getLogin());

        log.info("Token refresh completed successfully for user: {}", user.getLogin());
        return userMapper.toDtoUserResponse(user, newAccessToken, refreshToken);
    }

}