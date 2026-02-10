package com.example.bankcards.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.bankcards.dto.request.CreateRefreshTokenRequest;
import com.example.bankcards.exception.requestException.RolesEmptyException;
import com.example.bankcards.exception.userException.UserNullException;
import com.example.bankcards.mapper.RefreshTokenMapperImpl;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT token creation service.
 * 
 * Generates JWT access tokens and refresh tokens with proper claims,
 * expiration, and signing. Configures token properties based on
 * application settings.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCreator {

    @Value("${jwt.access-token-expiration}")
    private String accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private String refreshTokenExpiration;

    private final JwtHelper jwtHelper;
    private final RefreshTokenMapperImpl refreshTokenMapper;

    /**
     * Creates a JWT access token for a user.
     * 
     * Includes user claims (id, email, roles, login) and sets
     * expiration based on configured access token duration.
     * 
     * @param usersEntity user entity to create token for
     * @return signed JWT access token string
     * @throws UserNullException if user entity is null
     * @throws RolesEmptyException if user has no roles
     */
    public String createJwt(UsersEntity usersEntity) {
        log.info("Creating JWT access token for user: {}",
            usersEntity != null ? usersEntity.getLogin() : "null");

        if (usersEntity == null) {
            log.error("Cannot create JWT for null user");
            throw new UserNullException();
        }

        Instant now = Instant.now();
        List<String> roles = parseRoles(usersEntity.getRoles());
        log.debug("Parsed roles for JWT: {}", roles);

        long expirationSeconds = Integer.parseInt(accessTokenExpiration);
        Instant expirationTime = now.plusSeconds(expirationSeconds);
        log.trace("Token expiration: {} seconds from now", expirationSeconds);

        String jwt = Jwts.builder()
            .claim("id", usersEntity.getId())
            .claim("email", usersEntity.getEmail())
            .claim("role", roles)
            .claim("login", usersEntity.getLogin())
            .setIssuer("bankCards")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expirationTime))
            .signWith(jwtHelper.generateKey())
            .compact();

        log.info("JWT access token created for user: {}", usersEntity.getLogin());
        log.trace("Token length: {} chars", jwt.length());
        
        return jwt;
    }

    /**
     * Creates a refresh token request for a user.
     * 
     * Generates a longer-lived refresh token and packages it
     * with creation/expiration dates in a DTO.
     * 
     * @param user user entity to create refresh token for
     * @return refresh token request DTO
     * @throws UserNullException if user entity is null
     * @throws RolesEmptyException if user has no roles
     */
    public CreateRefreshTokenRequest createRefresh(UsersEntity user) {
        log.info("Creating refresh token for user: {}", 
            user != null ? user.getLogin() : "null");

        if (user == null) {
            log.error("Cannot create refresh token for null user");
            throw new UserNullException();
        }

        Instant now = Instant.now();
        long refreshExpirationSeconds = Integer.parseInt(refreshTokenExpiration);
        Instant expiredAt = now.plusSeconds(refreshExpirationSeconds);
        log.debug("Refresh token expiration: {} seconds from now", refreshExpirationSeconds);

        List<String> roles = parseRoles(user.getRoles());
        log.trace("Parsed roles for refresh token: {}", roles);

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

        log.info("Refresh token created for user: {}", user.getLogin());
        log.trace("Refresh token length: {} chars", refreshToken.length());

        CreateRefreshTokenRequest request = refreshTokenMapper.toDto(
            refreshToken,
            Date.from(now),
            Date.from(expiredAt)
        );
        
        log.debug("Refresh token request DTO created");
        return request;
    }

    /**
     * Parses user roles into string list for token claims.
     * 
     * @param roles set of role entities
     * @return list of role names as strings
     * @throws RolesEmptyException if no roles present
     */
    private List<String> parseRoles(Set<RoleEntity> roles) {
        log.trace("Parsing roles from set (size: {})", roles != null ? roles.size() : 0);

        if (roles == null || roles.isEmpty()) {
            log.error("User has no roles assigned");
            throw new RolesEmptyException();
        }

        List<String> roleNames = roles.stream()
            .map(role -> role.getRole().toString())
            .collect(Collectors.toList());

        log.trace("Parsed role names: {}", roleNames);
        return roleNames;
    }

}