package com.example.bankcards.service;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;

import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsersEntity user = userRepository.findByLogin(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("Username %s not found exception", username))
            );
        
        if (!user.isActive()) {
            log.error("User account is disabled: {}", username);
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }
        
        log.debug("User loaded successfully: {}", username);
        return user;
    }

}
