package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.bankcards.controller.interfaces.AuthorizationController;
import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.request.RefreshTokenRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.AuthenticationServiceImpl;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.service.UserRegistrationServiceImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthorizationControllerImpl implements AuthorizationController {

    private final UserRegistrationServiceImpl userRegistrationService;

    private final AuthenticationServiceImpl authenticationService;

    private final RefreshTokenService refreshTokenService;

    @Override
    @PostMapping("/signup")
    public UserResponse signup(@RequestBody RegistrationRequest registrationRequest) {
        return userRegistrationService.registerUser(registrationRequest);
    }

    @Override
    @PostMapping("/signin")
    public UserResponse signin(@RequestBody AuthorizationRequest authorizationRequest) {
        return authenticationService.authenticationUser(authorizationRequest);
    }

    @Override
    @PostMapping("/refresh")
    public UserResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return refreshTokenService.validateAndRefreshToken(refreshTokenRequest);
    }

}
