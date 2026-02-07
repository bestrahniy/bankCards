package com.example.bankcards.mapper;

import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.model.entity.UsersEntity;

@Component
public class UserMapper {

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

    public UserResponse toDto(UsersEntity usersEntity) {
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

    public UserResponse toDto(UsersEntity usersEntity, String jwtToken) {
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
            .build();
    }

}
