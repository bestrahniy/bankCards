package com.example.bankcards.mapper;

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
            .craetedAt(usersEntity.getCreatedAt())
            .roles(usersEntity.getRoles())
            .build();
    }

}
