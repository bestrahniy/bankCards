package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.userException.UserNotFoundException;
import com.example.bankcards.mapper.UserMapperImpl;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.interfaces.AdminUserService;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapperImpl userMapper;

    private final UsersRepository usersRepository;

    private final RoleRepository roleRepository;

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

        return userMapper.toDtoUserResponse(user);
    }

    @Transactional
    public UserActiveResponse blockUser(UUID userId) {
        UsersEntity usersEntity = usersRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        usersEntity.setActive(false);
        return userMapper.toDtoUserActiveResponse(usersEntity);
    }

    @Transactional
    public UserActiveResponse unblockUser(UUID userId) {
        UsersEntity usersEntity = usersRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        usersEntity.setActive(true);
        return userMapper.toDtoUserActiveResponse(usersEntity);
    }

}
