package com.example.bankcards.service;

import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.mapper.UserMapperImpl;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.interfaces.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for user registration and account creation.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserMapperImpl userMapper;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final UsersRepository usersRepository;

    /**
     * Registers a new user account in the system.
     * 
     * Validates registration data, encodes password, assigns default USER role,
     * and persists the new user entity. Returns created user details.
     * 
     * @param registrationRequest user registration data
     * @return response with created user details
     * @throws DataIntegrityViolationException if login/email already exists
     */
    @Transactional
    public UserResponse registerUser(RegistrationRequest registrationRequest) {
        log.info("Registering new user: {}", registrationRequest.getLogin());
        
        UsersEntity user = userMapper.toEntity(registrationRequest);
        log.debug("User entity mapped from request");

        user.setPassword(
            passwordEncoder.encode(
                registrationRequest.getPassword()
        ));
        log.trace("Password encoded securely");

        user.setRoles(
            roleRepository.findByRole(RoleType.USER)
                .stream()
                    .collect(Collectors.toSet())
        );
        log.debug("USER role assigned to new account");

        UsersEntity savedUser = usersRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toDtoUserResponse(savedUser);
    }

}