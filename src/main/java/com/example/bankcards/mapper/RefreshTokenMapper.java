package com.example.bankcards.mapper;

import java.util.Date;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.exception.RequestIsNullException;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;

@Component
public class RefreshTokenMapper {

    public CreateRefreshTokenRequest toDto(String refreshToken, Date createdAt, Date expiredAt) {
        return CreateRefreshTokenRequest.builder()
            .hashToken(refreshToken)
            .createdAt(createdAt)
            .expiredAt(expiredAt)
            .build();
    }

    public RefreshTokenEntity toEntity(CreateRefreshTokenRequest createRefreshTokenRequest, UsersEntity user) {
        if (createRefreshTokenRequest == null) {
            throw new RequestIsNullException(createRefreshTokenRequest);
        }

        return RefreshTokenEntity.builder()
            .hashToken(createRefreshTokenRequest.getHashToken())
            .createdAt(createRefreshTokenRequest.getCreatedAt())
            .expireAt(createRefreshTokenRequest.getExpiredAt())
            .user(user)
            .build();
    }

}
