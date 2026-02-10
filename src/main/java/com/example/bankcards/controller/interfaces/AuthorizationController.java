package com.example.bankcards.controller.interfaces;

import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.request.RefreshTokenRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * Interface for authorization-related operations
 * Provides endpoints for user registration, login, and token refresh
 */
public interface AuthorizationController {

    UserResponse signup(@RequestBody RegistrationRequest registrationRequest);

    UserResponse signin(@RequestBody AuthorizationRequest authorizationRequest);

    UserResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest);

}