package com.example.bankcards.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * 
 * Loads user details from database for authentication purposes.
 * Includes validation for user account active status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository userRepository;
    
    /**
     * Loads user details by username for authentication.
     * 
     * Retrieves user from database and verifies account is active.
     * Used by Spring Security during authentication process.
     * 
     * @param username login identifier to load
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found or inactive
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for: {}", username);
        
        UsersEntity user = userRepository.findByLogin(username)
            .orElseThrow(() -> {
                log.error("User not found during authentication: {}", username);
                return new UsernameNotFoundException(
                        String.format("Username %s not found exception", username));
            });
        log.trace("User found with ID: {}", user.getId());
        
        if (!user.isActive()) {
            log.error("User account is disabled: {}", username);
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }
        
        log.debug("User loaded successfully: {}", username);
        return user;
    }

}