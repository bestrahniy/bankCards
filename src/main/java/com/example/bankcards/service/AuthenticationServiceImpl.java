package com.example.bankcards.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.userException.UserNotFoundException;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.mapper.UserMapperImpl;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.interfaces.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication service for user login and credential validation.
 * 
 * Handles user authentication using Spring Security's AuthenticationManager,
 * generates JWT tokens, and creates refresh tokens for session management.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UsersRepository usersRepository;
    private final JwtCreator jwtCreator;
    private final RefreshTokenService refreshTokenService;
    private final UserMapperImpl userMapper;

    /**
     * Authenticates user credentials and generates authentication tokens.
     * 
     * Validates username/password combination, then generates
     * JWT access token and refresh token for the authenticated session.
     * 
     * @param authorizationRequest contains login credentials
     * @return user response with authentication tokens
     * @throws UsernameNotFoundException if credentials are invalid
     * @throws UserNotFoundException if user doesn't exist
     */
    @Transactional
    public UserResponse authenticationUser(AuthorizationRequest authorizationRequest) {
        log.info("Starting authentication for user: {}", authorizationRequest.getLogin());
        
        try {
            log.debug("Authenticating credentials via AuthenticationManager...");
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authorizationRequest.getLogin(),
                    authorizationRequest.getPassword()
                )
            );
            log.debug("Authentication successful for: {}", authorizationRequest.getLogin());
        } catch (AuthenticationException authenticationException) {
            log.error("Authentication failed for user: {}", authorizationRequest.getLogin(), authenticationException);
            throw authenticationException;
        }

        UsersEntity user = usersRepository.findByLogin(authorizationRequest.getLogin())
            .orElseThrow(() -> {
                log.error("User not found after authentication: {}", authorizationRequest.getLogin());
                return new UserNotFoundException(authorizationRequest.getLogin());
            });
        log.debug("User loaded from repository: {}", user.getLogin());

        log.trace("Generating JWT token...");
        String jwtToken = jwtCreator.createJwt(user);
        log.trace("JWT token generated successfully");

        log.trace("Creating refresh token...");
        RefreshTokenEntity refreshToken = refreshTokenService.createRefershToken(user);
        log.debug("Refresh token created with ID: {}", refreshToken.getId());

        log.info("Authentication completed successfully for: {}", user.getLogin());
        return userMapper.toDtoUserResponse(user, jwtToken, refreshToken);
    }

}