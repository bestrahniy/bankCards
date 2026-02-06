package com.example.bankcards.service;

import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    @Transactional
    public UserResponse registerUser(RegistrationRequest registrationRequest) {
        UsersEntity user = userMapper.toEntity(registrationRequest);

        user.setPassword(
            passwordEncoder.encode(
                registrationRequest.getPassword()
        ));

        user.setRoles(
            roleRepository.findByRole(RoleType.USER)
                .stream()
                    .collect(Collectors.toSet())
        );

        usersRepository.save(user);

        return userMapper.toDto(user);
    }

}
