package com.example.bankcards.jwt;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.example.bankcards.model.entity.UsersEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT utility helper for token operations.
 * 
 * Provides methods for JWT token validation, claim extraction,
 * and key generation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHelper {

    @Value("${jwt.secretKey}")
    private String secretKey;

    /**
     * Generates signing key from Base64-encoded secret.
     * 
     * @return HMAC SHA key for JWT signing/verification
     */
    private SecretKey getSigningKey() {
        log.trace("Generating signing key from secret");
        byte[] bytesKey = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytesKey);
    }

    /**
     * Generates key for JWT operations.
     * 
     * @return secret key for token signing
     */
    public SecretKey generateKey() {
        log.trace("Generating JWT key");
        return getSigningKey();
    }

    /**
     * Validates JWT token against user details.
     * 
     * @param token JWT token to validate
     * @param userDetails user details to verify against
     * @return true if token is valid and not expired
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        log.debug("Validating token against UserDetails for: {}",
            userDetails != null ? userDetails.getUsername() : "null");
        
        try {
            final String username = extractLogin(token);
            boolean isValid = (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            
            log.debug("Token validation result: {} for user: {}", isValid, username);
            return isValid;
            
        } catch (Exception exception) {
            log.error("Token validation failed: {}", exception.getMessage());
            return false;
        }
    }

    /**
     * Validates JWT token against user entity.
     * 
     * @param token JWT token to validate
     * @param usersEntity user entity to verify against
     * @return true if token is valid and not expired
     */
    public Boolean validateToken(String token, UsersEntity usersEntity) {
        log.debug("Validating token against UsersEntity for: {}",
            usersEntity != null ? usersEntity.getLogin() : "null");
        
        try {
            final String username = extractLogin(token);
            boolean isValid = (username != null && username.equals(usersEntity.getLogin()) && !isTokenExpired(token));
            
            log.debug("Token validation result: {} for user: {}", isValid, username);
            return isValid;
            
        } catch (Exception exception) {
            log.error("Token validation failed: {}", exception.getMessage());
            return false;
        }
    }

    /**
     * Extracts login from JWT token.
     * 
     * @param token JWT token to parse
     * @return extracted username or null if extraction fails
     */
    public String extractLogin(String token) {
        log.trace("Extracting login from JWT token");
        
        try {
            Claims claims = extractClaims(token);
            String login = claims.get("login", String.class);
            log.trace("Extracted login: {}", login);
            return login;
            
        } catch (Exception exception) {
            log.error("Failed to extract login from token: {}", exception.getMessage());
            return null;
        }
    }

    /**
     * Extracts expiration date from JWT token.
     * 
     * @param token JWT token to parse
     * @return expiration date or null if extraction fails
     */
    public Date extractExpiration(String token) {
        log.trace("Extracting expiration from JWT token");
        
        try {
            Date expiration = extractClaims(token).getExpiration();
            log.trace("Extracted expiration: {}", expiration);
            return expiration;
            
        } catch (Exception exception) {
            log.error("Failed to extract expiration date: {}", exception.getMessage());
            return null;
        }
    }

    /**
     * Extracts all claims from JWT token.
     * 
     * @param token JWT token to parse
     * @return token claims
     * @throws JwtException if token parsing fails
     */
    private Claims extractClaims(String token) {
        log.trace("Extracting claims from JWT token");
        
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Checks if JWT token is expired.
     * 
     * @param token JWT token to check
     * @return true if token is expired
     */
    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        if (expiration == null) {
            log.warn("Could not determine token expiration");
            return true;
        }
        
        boolean expired = expiration.before(new Date());
        log.trace("Token expired check: {} (expiration: {}, now: {})",
            expired, expiration, new Date());
        
        return expired;
    }

}