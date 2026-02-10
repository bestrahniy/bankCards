package com.example.bankcards.controller.interfaces;

import org.springframework.web.bind.annotation.RequestBody;
import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.request.RefreshTokenRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;

public interface AuthorizationController {

    UserResponse signup(@RequestBody RegistrationRequest registrationRequest);

    UserResponse signin(@RequestBody AuthorizationRequest authorizationRequest);

    UserResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest);

}
