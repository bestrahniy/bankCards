package com.example.bankcards.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.exception.userException.UserNotFoundException;
import com.example.bankcards.jwt.JwtCreator;
import com.example.bankcards.mapper.UserMapperImpl;
import com.example.bankcards.model.entity.RefreshTokenEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.service.interfaces.AuthenticationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UsersRepository usersRepository;

    private final JwtCreator jwtCreator;

    private final RefreshTokenService refreshTokenService;

    private final UserMapperImpl userMapper;

    @Transactional
    public UserResponse authenticationUser(AuthorizationRequest authorizationRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authorizationRequest.getLogin(),
                authorizationRequest.getPassword()
            )
        );

        UsersEntity user = usersRepository.findByLogin(authorizationRequest.getLogin())
            .orElseThrow(() -> new UserNotFoundException(authorizationRequest.getLogin()));

        String jwtToken = jwtCreator.createJwt(user);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefershToken(user);

        return userMapper.toDtoUserResponse(user, jwtToken, refreshToken);
    }

}
