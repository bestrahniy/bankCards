package com.example.bankcards.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;

    private final UsersRepository usersRepository;

    private final RoleRepository roleRepository;

    @SuppressWarnings("null")
    @Transactional
    public UserResponse grantAdminRole(UUID userId) {
        UsersEntity user = usersRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Set<RoleEntity> roles = user.getRoles();

        List<RoleEntity> adminRoles = roleRepository.findByRole(RoleType.ADMIN);

        if (adminRoles.isEmpty()) {
            RoleEntity newAdminRole = RoleEntity.builder()
                    .role(RoleType.ADMIN)
                    .build();
            RoleEntity savedAdminRole = roleRepository.save(newAdminRole);
            roles.add(savedAdminRole);
        } else {
            roles.add(adminRoles.get(0));
        }

        user.setRoles(roles);
        usersRepository.save(user);

        return userMapper.toDto(user);
    }

}
