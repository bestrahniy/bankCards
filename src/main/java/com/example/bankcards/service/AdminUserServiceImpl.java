package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

/**
 * Service for administrative user management operations.
 * Provides functionality for managing user accounts at administrative level
 * All operations require ADMIN privileges and affect user permissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapperImpl userMapper;
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;

    /**
     * Grants administrator role to a user.
     * 
     * Adds ADMIN role to user's existing roles. If ADMIN role
     * doesn't exist in system, creates it first.
     * 
     * @param userId unique identifier of user to promote
     * @return response with updated user details
     * @throws UserNotFoundException if user doesn't exist
     */
    @Transactional
    public UserResponse grantAdminRole(UUID userId) {
        log.info("Granting ADMIN role to user ID: {}", userId);
        
        UsersEntity user = usersRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found for role grant: {}", userId);
                return new UserNotFoundException(userId);
            });
        log.debug("User found: {}", user.getLogin());

        Set<RoleEntity> roles = user.getRoles();
        log.trace("Current user roles: {}", 
            roles.stream().map(r -> r.getRole().name()).collect(Collectors.toList()));

        List<RoleEntity> adminRoles = roleRepository.findByRole(RoleType.ADMIN);
        
        if (adminRoles.isEmpty()) {
            log.info("ADMIN role not found in system, creating...");
            RoleEntity newAdminRole = RoleEntity.builder()
                    .role(RoleType.ADMIN)
                    .build();
            RoleEntity savedAdminRole = roleRepository.save(newAdminRole);
            roles.add(savedAdminRole);
            log.debug("New ADMIN role created with ID: {}", savedAdminRole.getId());
        } else {
            roles.add(adminRoles.get(0));
            log.debug("Existing ADMIN role added to user");
        }

        user.setRoles(roles);
        usersRepository.save(user);
        log.info("ADMIN role granted successfully to user: {}", user.getLogin());

        return userMapper.toDtoUserResponse(user);
    }

    /**
     * Blocks a user account preventing authentication.
     * 
     * Sets user's active status to false. User cannot login
     * or perform any operations while blocked.
     * 
     * @param userId unique identifier of user to block
     * @return response with updated user status
     * @throws UserNotFoundException if user doesn't exist
     */
    @Transactional
    public UserActiveResponse blockUser(UUID userId) {
        log.warn("Blocking user account ID: {}", userId);
        
        UsersEntity usersEntity = usersRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found for blocking: {}", userId);
                return new UserNotFoundException(userId);
            });
        
        usersEntity.setActive(false);
        log.info("User account blocked: {}", usersEntity.getLogin());
        
        return userMapper.toDtoUserActiveResponse(usersEntity);
    }

    /**
     * Unblocks a previously blocked user account.
     * 
     * Restores user's active status to true, allowing
     * authentication and normal operations.
     * 
     * @param userId unique identifier of user to unblock
     * @return response with updated user status
     * @throws UserNotFoundException if user doesn't exist
     */
    @Transactional
    public UserActiveResponse unblockUser(UUID userId) {
        log.info("Unblocking user account ID: {}", userId);
        
        UsersEntity usersEntity = usersRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found for unblocking: {}", userId);
                return new UserNotFoundException(userId);
            });
        
        usersEntity.setActive(true);
        log.info("User account unblocked: {}", usersEntity.getLogin());
        
        return userMapper.toDtoUserActiveResponse(usersEntity);
    }

}