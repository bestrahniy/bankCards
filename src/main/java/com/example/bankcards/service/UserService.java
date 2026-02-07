package com.example.bankcards.service;

import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.mapper.NotificationMapper;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.model.entity.NotificationEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.NotificationRepository;
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

    private final AuthenticationManager authenticationManager;

    private final JwtCreator jwtCreator;

    private final RefreshTokenService refreshTokenService;

    private final NotificationMapper notificationMapper;

    private final NotificationRepository notificationRepository;

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

    public UserResponse authorizationUser(AuthorizationRequest authorizationRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authorizationRequest.getLogin(),
                authorizationRequest.getPassword()
            )
        );

        UsersEntity user = usersRepository.findByLogin(authorizationRequest.getLogin())
            .orElseThrow(() -> new UserNotFoundException(authorizationRequest.getLogin()));

        String jwtToken = jwtCreator.createJwt(user);
        refreshTokenService.createRefershToken(user);

        return userMapper.toDto(user, jwtToken);
    }

    @Transactional
    public NotificationResponse createCardRequest(UserDetails userDetails) {
        UsersEntity user = usersRepository.findByLoginWithRoles(userDetails.getUsername())
            .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));

        NotificationEntity notificationEntity = notificationMapper.toEntity(user);
        notificationRepository.save(notificationEntity);
        return notificationMapper.toDto(user, notificationEntity);
    }

}
