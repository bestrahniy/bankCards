package com.example.bankcards.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.exception.RolesEmptyException;
import com.example.bankcards.exception.UserEntityNullException;
import com.example.bankcards.mapper.RefreshTokenMapper;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtCreator {

    @Value("${jwt.access-token-expiration}")
    private String accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private String refreshTokenExpiration;

    private final JwtHelper jwtHelper;

    private final RefreshTokenMapper refreshTokenMapper;

    public String createJwt(UsersEntity usersEntity) {

        if (usersEntity == null) {
            throw new UserEntityNullException();
        }

        Instant now = Instant.now();
        List<String> roles = parseRoles(usersEntity.getRoles());

        return Jwts.builder()
            .claim("id", usersEntity.getId())
            .claim("email", usersEntity.getEmail())
            .claim("role", roles)
            .claim("login", usersEntity.getLogin())
            .setIssuer("bankCards")
            .setIssuedAt(Date.from(now))
            .setExpiration(
                Date.from(
                    now.plusSeconds(
                        Integer.parseInt(accessTokenExpiration)
                    )
                )
            )
            .signWith(jwtHelper.generateKey())
            .compact();
    }

    public CreateRefreshTokenRequest createRefresh(UsersEntity user) {
        if (user == null) {
            throw new UserEntityNullException();
        }

        Instant now = Instant.now();
        Instant expiredAt = now.plusSeconds(Integer.parseInt(refreshTokenExpiration));
        List<String> roles = parseRoles(user.getRoles());

        String refreshToken = Jwts.builder()
            .claim("id", user.getId())
            .claim("email", user.getEmail())
            .claim("role", roles)
            .claim("login", user.getLogin())
            .setIssuer("bankCards")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiredAt))
            .signWith(jwtHelper.generateKey())
            .compact();

        return refreshTokenMapper.toDto(
            refreshToken,
            Date.from(now),
            Date.from(expiredAt)
        );
    }

    private List<String> parseRoles(Set<RoleEntity> roles) {
        if (roles.isEmpty()) {
            throw new RolesEmptyException();
        }

        return roles.stream()
            .map(role -> role.getRole().toString())
            .collect(Collectors.toList());
    }

}
