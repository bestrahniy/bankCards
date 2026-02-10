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

@RequiredArgsConstructor
@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserMapperImpl userMapper;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final UsersRepository usersRepository;

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

        return userMapper.toDtoUserResponse(user);
    }

}
