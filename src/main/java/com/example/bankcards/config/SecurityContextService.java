package com.example.bankcards.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;

@Service
@Transactional(readOnly = true)
public class SecurityContextService {

    @Autowired
    private UsersRepository usersRepository;

    public UsersEntity getCurrentUser() {
        return usersRepository.findByLogin(getAuthentication().getName())
            .orElseThrow(() -> new UserNotFoundException(getAuthentication().getName()));
    }

    public Boolean isCurrentActive() {
        return getCurrentUser().isActive();
    }

    public String getLogin() {
        return getAuthentication().getName();
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
