package com.example.bankcards.unit.service;

import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsersRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWhenUserIsActive() {
        UsersEntity user = UsersEntity.builder()
                .login("testuser")
                .password("password")
                .isActive(true)
                .build();

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        assertThat(result).isSameAs(user);
        verify(userRepository).findByLogin("testuser");
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });

        assertThat(exception.getMessage()).contains("Username nonexistent not found exception");
        verify(userRepository).findByLogin("nonexistent");
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserIsInactive() {
        UsersEntity user = UsersEntity.builder()
                .login("inactiveuser")
                .password("password")
                .isActive(false)
                .build();

        when(userRepository.findByLogin("inactiveuser")).thenReturn(Optional.of(user));

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("inactiveuser");
        });

        assertThat(exception.getMessage()).contains("User account is disabled: inactiveuser");
        verify(userRepository).findByLogin("inactiveuser");
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWithCorrectMessageForNotFound() {
        String username = "unknownuser";
        when(userRepository.findByLogin(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });

        assertThat(exception.getMessage()).isEqualTo(String.format("Username %s not found exception", username));
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWithCorrectMessageForInactive() {
        String username = "disableduser";
        UsersEntity user = UsersEntity.builder()
                .login(username)
                .password("pass")
                .isActive(false)
                .build();

        when(userRepository.findByLogin(username)).thenReturn(Optional.of(user));

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });

        assertThat(exception.getMessage()).isEqualTo("User account is disabled: " + username);
    }

    @Test
    void loadUserByUsername_shouldHandleEmptyOptional() {
        String username = "emptyuser";
        when(userRepository.findByLogin(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });

        verify(userRepository).findByLogin(username);
    }

    @Test
    void loadUserByUsername_shouldCallRepositoryExactlyOnce() {
        UsersEntity user = UsersEntity.builder()
                .login("singlecalluser")
                .password("pass")
                .isActive(true)
                .build();

        when(userRepository.findByLogin("singlecalluser")).thenReturn(Optional.of(user));

        customUserDetailsService.loadUserByUsername("singlecalluser");

        verify(userRepository, times(1)).findByLogin("singlecalluser");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserWithRoles() {
        UsersEntity user = UsersEntity.builder()
                .login("roleuser")
                .password("pass")
                .isActive(true)
                .build();

        when(userRepository.findByLogin("roleuser")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("roleuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("roleuser");
        verify(userRepository).findByLogin("roleuser");
    }

}