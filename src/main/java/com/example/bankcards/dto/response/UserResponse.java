package com.example.bankcards.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.example.bankcards.model.entity.RoleEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private UUID id;

    private String login;

    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, 
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            timezone = "UTC")
    private Instant craetedAt;

    private Set<RoleEntity> roles;

}
