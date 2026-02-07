package com.example.bankcards.unit.controller;


import com.example.bankcards.controller.UserController;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private RegistrationRequest validRegistrationRequest;
    private UserResponse expectedUserResponse;

    @BeforeEach
    void setUp() {
        validRegistrationRequest = RegistrationRequest.builder()
                .login("testuser")
                .password("password123")
                .email("test@example.com")
                .build();

        expectedUserResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .email("test@example.com")
                .createdAt(Instant.now())
                .roles(null)
                .build();
    }

    @Test
    @DisplayName("POST /api/user/signup: должен успешно регистрировать пользователя")
    void signup_shouldRegisterUserSuccessfully() {
        // Given
        when(userService.registerUser(any(RegistrationRequest.class)))
                .thenReturn(expectedUserResponse);

        // When
        UserResponse response = userController.signup(validRegistrationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(expectedUserResponse.getId());
        assertThat(response.getLogin()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        
        verify(userService, times(1)).registerUser(validRegistrationRequest);
        verifyNoMoreInteractions(userService);
    }
}