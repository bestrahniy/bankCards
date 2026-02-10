package com.example.bankcards.mapper.interfaces.specializedInterface;

import java.util.Date;

import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;

public interface RefreshTokenMapper {
    CreateRefreshTokenRequest toDto(String refreshToken, Date createdAt, Date expiredAt);

    RefreshTokenEntity toEntity(CreateRefreshTokenRequest createRefreshTokenRequest, UsersEntity user);

}
