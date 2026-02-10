package com.example.bankcards.mapper;

import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.mapper.interfaces.specializedInterface.UserMapper;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;

@Component
public class UserMapperImpl implements UserMapper {

    public UsersEntity toEntity(RegistrationRequest registrationRequest) {
        if (registrationRequest == null) {
            return null;
        }

        return UsersEntity.builder()
            .login(registrationRequest.getLogin())
            .email(registrationRequest.getEmail())
            .createdAt(Instant.now())
            .build();
    }

    public UserResponse toDtoUserResponse(UsersEntity usersEntity) {
        if (usersEntity == null) {
            return null;
        }

        return UserResponse.builder()
            .id(usersEntity.getId())
            .login(usersEntity.getLogin())
            .email(usersEntity.getEmail())
            .createdAt(usersEntity.getCreatedAt())
            .roles(usersEntity.getRoles().stream()
                    .map(role -> role.toString())
                    .collect(Collectors.toSet()))
            .jwt(null)
            .build();
    }

    public UserResponse toDtoUserResponse(UsersEntity usersEntity,
        String jwtToken, RefreshTokenEntity refreshTokenEntity
    ) {
        if (usersEntity == null) {
            return null;
        }

        return UserResponse.builder()
            .id(usersEntity.getId())
            .login(usersEntity.getLogin())
            .email(usersEntity.getEmail())
            .createdAt(usersEntity.getCreatedAt())
            .roles(usersEntity.getRoles().stream()
                    .map(role -> role.toString())
                    .collect(Collectors.toSet()))
            .jwt(jwtToken)
            .refreshToken(refreshTokenEntity.getHashToken())
            .build();
    }

    public UserActiveResponse toDtoUserActiveResponse(UsersEntity usersEntity) {
        return UserActiveResponse.builder()
            .login(usersEntity.getLogin())
            .isActive(usersEntity.isActive())
            .build();
    }

}
