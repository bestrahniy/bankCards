package com.example.bankcards.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorizationRequest {

    @NonNull
    private String login;

    @NonNull
    private String password;

}
