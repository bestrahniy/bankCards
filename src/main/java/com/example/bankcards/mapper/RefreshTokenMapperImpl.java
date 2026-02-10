package com.example.bankcards.mapper;

import java.util.Date;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.exception.requestException.RequestIsNullException;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper for RefreshTokenEntity transformations.
 * 
 * Handles conversion between refresh token entities and DTOs.
 */
@Component
@Slf4j
public class RefreshTokenMapperImpl {

    /**
     * Creates CreateRefreshTokenRequest DTO from token data.
     * 
     * @param refreshToken JWT refresh token string
     * @param createdAt token creation timestamp
     * @param expiredAt token expiration timestamp
     * @return refresh token request DTO
     */
    public CreateRefreshTokenRequest toDto(String refreshToken, Date createdAt, Date expiredAt) {
        log.debug("Creating CreateRefreshTokenRequest DTO");
        
        return CreateRefreshTokenRequest.builder()
            .hashToken(refreshToken)
            .createdAt(createdAt)
            .expiredAt(expiredAt)
            .build();
    }

    /**
     * Creates RefreshTokenEntity from DTO and user.
     * 
     * @param createRefreshTokenRequest refresh token request DTO
     * @param user associated user entity
     * @return refresh token entity
     * @throws RequestIsNullException if request DTO is null
     */
    public RefreshTokenEntity toEntity(CreateRefreshTokenRequest createRefreshTokenRequest, UsersEntity user) {
        log.debug("Creating RefreshTokenEntity from DTO for user: {}", 
            user != null ? user.getLogin() : "null");
        
        if (createRefreshTokenRequest == null) {
            log.error("CreateRefreshTokenRequest is null");
            throw new RequestIsNullException(createRefreshTokenRequest);
        }

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
            .hashToken(createRefreshTokenRequest.getHashToken())
            .createdAt(createRefreshTokenRequest.getCreatedAt())
            .expireAt(createRefreshTokenRequest.getExpiredAt())
            .user(user)
            .build();
        
        log.trace("Refresh token entity created, expires at: {}", entity.getExpireAt());
        return entity;
    }

}
