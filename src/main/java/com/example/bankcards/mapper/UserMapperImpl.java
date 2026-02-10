package com.example.bankcards.mapper;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.mapper.interfaces.specializedInterface.UserMapper;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * Mapper for UsersEntity transformations and DTO conversions.
 *
 * Handles mapping between user entities and various user-related DTOs.
 */
@Component
@Slf4j
public class UserMapperImpl implements UserMapper {

    /**
     * Creates UsersEntity from RegistrationRequest DTO.
     * 
     * @param registrationRequest user registration data
     * @return new user entity
     */
    public UsersEntity toEntity(RegistrationRequest registrationRequest) {
        log.debug("Creating UsersEntity from RegistrationRequest");
        
        if (registrationRequest == null) {
            log.warn("RegistrationRequest is null, returning null entity");
            return null;
        }

        UsersEntity entity = UsersEntity.builder()
            .login(registrationRequest.getLogin())
            .email(registrationRequest.getEmail())
            .createdAt(Instant.now())
            .build();
        
        log.trace("User entity created for login: {}", registrationRequest.getLogin());
        return entity;
    }

    /**
     * Maps UsersEntity to UserResponse DTO without authentication tokens.
     * 
     * @param usersEntity source user entity
     * @return user response DTO
     */
    public UserResponse toDtoUserResponse(UsersEntity usersEntity) {
        log.debug("Mapping UsersEntity to UserResponse (without tokens)");
        
        if (usersEntity == null) {
            log.warn("UsersEntity is null, returning null response");
            return null;
        }

        Set<String> roles = usersEntity.getRoles().stream()
                .map(role -> role.getRole().toString())
                .collect(Collectors.toSet());
        
        log.trace("User roles mapped: {}", roles);

        return UserResponse.builder()
            .id(usersEntity.getId())
            .login(usersEntity.getLogin())
            .email(usersEntity.getEmail())
            .createdAt(usersEntity.getCreatedAt())
            .roles(roles)
            .jwt(null)
            .build();
    }

    /**
     * Maps UsersEntity to UserResponse DTO with authentication tokens.
     * 
     * @param usersEntity source user entity
     * @param jwtToken JWT access token
     * @param refreshTokenEntity refresh token entity
     * @return user response DTO with tokens
     */
    public UserResponse toDtoUserResponse(UsersEntity usersEntity,
        String jwtToken, RefreshTokenEntity refreshTokenEntity
    ) {
        log.debug("Mapping UsersEntity to UserResponse (with tokens)");
        
        if (usersEntity == null) {
            log.warn("UsersEntity is null, returning null response");
            return null;
        }

        Set<String> roles = usersEntity.getRoles().stream()
                .map(role -> role.getRole().toString())
                .collect(Collectors.toSet());
        
        log.trace("User roles mapped: {}, JWT present: {}, Refresh token present: {}", 
            roles.size(), jwtToken != null, refreshTokenEntity != null);

        return UserResponse.builder()
            .id(usersEntity.getId())
            .login(usersEntity.getLogin())
            .email(usersEntity.getEmail())
            .createdAt(usersEntity.getCreatedAt())
            .roles(roles)
            .jwt(jwtToken)
            .refreshToken(refreshTokenEntity.getHashToken())
            .build();
    }

    /**
     * Maps UsersEntity to UserActiveResponse DTO.
     * 
     * @param usersEntity source user entity
     * @return user active status response DTO
     */
    public UserActiveResponse toDtoUserActiveResponse(UsersEntity usersEntity) {
        log.debug("Mapping UsersEntity to UserActiveResponse");
        
        return UserActiveResponse.builder()
            .login(usersEntity.getLogin())
            .isActive(usersEntity.isActive())
            .build();
    }

}