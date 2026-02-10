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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation if AuthorizationController
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthorizationControllerImpl implements AuthorizationController {

    private final UserRegistrationServiceImpl userRegistrationService;

    private final AuthenticationServiceImpl authenticationService;

    private final RefreshTokenService refreshTokenService;

    /**
     * Registers a new user
     *
     * @param registrationRequest Request containing registration details
     * @return UserResponse with new user details
     */
    @Operation(
            summary = "User signup",
            description = "Registers a new user with the provided credentials",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid registration data"),
                    @ApiResponse(responseCode = "409", description = "User already exists"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PostMapping("/signup")
    public UserResponse signup(@RequestBody RegistrationRequest registrationRequest) {
        log.info("Entering signup with request: {}", registrationRequest);
        UserResponse response = userRegistrationService.registerUser(registrationRequest);
        log.info("Exiting signup with response: {}", response);
        return response;
    }

    /**
     * Authenticates a user
     *
     * @param authorizationRequest Request containing login credentials
     * @return UserResponse with authenticated user details and JWT
     */
    @Operation(
            summary = "User signin",
            description = "Authenticates the user and returns a JWT token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - wrong credentials"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PostMapping("/signin")
    public UserResponse signin(@RequestBody AuthorizationRequest authorizationRequest) {
        log.info("Entering signin with request: {}", authorizationRequest);
        UserResponse response = authenticationService.authenticationUser(authorizationRequest);
        log.info("Exiting signin with response: {}", response);
        return response;
    }

    /**
     * Refreshes the JWT token using a refresh token
     *
     * @param refreshTokenRequest Request containing refresh token
     * @return UserResponse with new JWT
     */
    @Operation(
            summary = "Refresh JWT token",
            description = "Validates the refresh token and issues a new JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PostMapping("/refresh")
    public UserResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Entering refreshToken with request: {}", refreshTokenRequest);
        UserResponse response = refreshTokenService.validateAndRefreshToken(refreshTokenRequest);
        log.info("Exiting refreshToken with response: {}", response);
        return response;
    }

}
