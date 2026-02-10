package com.example.bankcards.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    @NonNull
    private UUID id;

    @NonNull
    private String login;

    @NonNull
    private String email;

    @NonNull
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            timezone = "UTC")
    private Instant createdAt;

    @NonNull
    private Set<String> roles;

    private String jwt;

    private String refreshToken;

}
