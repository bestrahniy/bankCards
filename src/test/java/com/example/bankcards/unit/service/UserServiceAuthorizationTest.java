package com.example.bankcards.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.userException.UserNotFoundException;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthorizationTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private JwtCreator jwtCreator;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Test
    void authorizationUser_WithValidCredentials_ShouldReturnUserResponseWithJwt() {
        AuthorizationRequest request = AuthorizationRequest.builder()
                .login("testuser")
                .password("password123")
                .build();
        
        UsersEntity user = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .email("test@example.com")
                .password("encodedPass")
                .isActive(true)
                .build();
        
        UserResponse expectedResponse = UserResponse.builder()
                .id(user.getId())
                .login(user.getLogin())
                .jwt("jwt-token")
                .build();
        
        when(usersRepository.findByLogin("testuser")).thenReturn(Optional.of(user));
        when(jwtCreator.createJwt(user)).thenReturn("jwt-token");
        when(userMapper.toDtoUserResponse(user, "jwt-token")).thenReturn(expectedResponse);

        UserResponse result = userService.authorizationUser(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals("jwt-token", result.getJwt());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(usersRepository, times(1)).findByLogin("testuser");
        verify(jwtCreator, times(1)).createJwt(user);
        verify(refreshTokenService, times(1)).createRefershToken(user);
        verify(userMapper, times(1)).toDtoUserResponse(user, "jwt-token");
    }

    @Test
    void authorizationUser_WithInvalidLogin_ShouldThrowUserNotFoundException() {
        AuthorizationRequest request = AuthorizationRequest.builder()
                .login("nonexistent")
                .password("password")
                .build();
        
        when(usersRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.authorizationUser(request));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(usersRepository, times(1)).findByLogin("nonexistent");
        verify(jwtCreator, never()).createJwt(any());
        verify(refreshTokenService, never()).createRefershToken(any());
        verify(userMapper, never()).toDtoUserResponse(any(), any());
    }

    @Test
    void authorizationUser_WithInactiveUser_ShouldThrowUserNotFoundException() {
        AuthorizationRequest request = AuthorizationRequest.builder()
                .login("inactive")
                .password("password")
                .build();
        
        UsersEntity inactiveUser = UsersEntity.builder()
                .id(UUID.randomUUID())
                .login("inactive")
                .isActive(false)
                .build();
        
        when(usersRepository.findByLogin("inactive")).thenReturn(Optional.of(inactiveUser));

        assertThrows(UserNotFoundException.class, () -> userService.authorizationUser(request));
    }
}