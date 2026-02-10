package com.example.bankcards.mapper.interfaces.specializedInterface;

import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;

public interface UserMapper {

    UsersEntity toEntity(RegistrationRequest registrationRequest);

    UserResponse toDtoUserResponse(UsersEntity usersEntity);

    UserResponse toDtoUserResponse(UsersEntity usersEntity,
        String jwtToken, RefreshTokenEntity refreshTokenEntity);

    UserActiveResponse toDtoUserActiveResponse(UsersEntity usersEntity);

}
