package com.example.bankcards.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.exception.userException.UserNotFoundException;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for accessing Spring Security context information.
 * 
 * Provides methods to retrieve current authenticated user details
 * and security context information. .
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class SecurityContextService {

    @Autowired
    private UsersRepository usersRepository;

    /**
     * Retrieves the currently authenticated user entity.
     * 
     * Fetches user from database based on Spring Security authentication.
     * Used throughout application to get current user context.
     * 
     * @return current authenticated user entity
     * @throws UserNotFoundException if user doesn't exist in database
     */
    public UsersEntity getCurrentUser() {
        String username = getAuthentication().getName();
        log.debug("Retrieving current user: {}", username);
        
        return usersRepository.findByLogin(username)
            .orElseThrow(() -> {
                log.error("Current user not found in database: {}", username);
                return new UserNotFoundException(username);
            });
    }

    /**
     * Checks if current user account is active.
     * 
     * @return true if user account is active, false if suspended
     */
    public Boolean isCurrentActive() {
        boolean isActive = getCurrentUser().isActive();
        log.trace("Current user active status: {}", isActive);
        return isActive;
    }

    /**
     * Retrieves login/username of current authenticated user.
     * 
     * @return current user's login
     */
    public String getLogin() {
        String login = getAuthentication().getName();
        log.trace("Current user login: {}", login);
        return login;
    }

    /**
     * Retrieves Spring Security Authentication object.
     * 
     * @return current authentication context
     */
    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("No authentication found in SecurityContext");
        }
        return auth;
    }
}