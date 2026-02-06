package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationRequest {

    @NonNull
    @Size(min = 5, max = 20)
    private String login;

    @NonNull
    @Size(min = 8)
    private String password;

    @NonNull
    @Email
    private String email;

}
