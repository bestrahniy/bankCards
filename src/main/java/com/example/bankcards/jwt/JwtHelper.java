package com.example.bankcards.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHelper {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private SecretKey getSigningKey() {
        byte[] bytesKey = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytesKey);
    }

    public SecretKey generateKey() {
        return getSigningKey();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractLogin(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception exception) {
            log.error("Token validation failed: {}", exception.getMessage());
            return false;
        }
    }

    public String extractLogin(String token) {
        try{
            Claims claims = extractClaims(token);
            return claims.get("login", String.class);
        } catch (Exception exception) {
            log.error("Failed to extract login: {}", exception.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaims(token).getExpiration();
        } catch (Exception exception) {
            log.error("Failed to extract expiration date: {}", exception.getMessage());
            return null;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Boolean isTokenExpired(String token) {
        if (extractExpiration(token).before(new Date())) {
            return true;
        }
        return false;
    }

}
