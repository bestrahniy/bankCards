package com.example.bankcards.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.bankcards.exception.RolesEmptyException;
import com.example.bankcards.exception.UserEntityNullException;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtCreator {

    @Value("${jwt.access-token-expiration}")
    private String accessTokenAxparation;

    private final JwtHelper jwtHelper;

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
                        Integer.parseInt(accessTokenAxparation)
                    )
                )
            )
            .signWith(jwtHelper.generateKey())
            .compact();
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
