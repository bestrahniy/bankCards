package com.example.bankcards.unit.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.bankcards.jwt.JwtHelper;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtHelperTest {

    @InjectMocks
    private JwtHelper jwtHelper;

    @Mock
    private UserDetails userDetails;

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtHelper, "secretKey", SECRET_KEY);
    }

    @Test
    void generateKey_shouldReturnSecretKey() {
        SecretKey result = jwtHelper.generateKey();

        assertThat(result).isNotNull();
        byte[] expectedBytes = Decoders.BASE64.decode(SECRET_KEY);
        SecretKey expectedKey = Keys.hmacShaKeyFor(expectedBytes);
        assertThat(result).isEqualTo(expectedKey);
    }

    @Test
    void getSigningKey_shouldReturnKeyFromSecret() {
        SecretKey result = jwtHelper.generateKey();

        assertThat(result).isNotNull();
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = createValidToken();
        when(userDetails.getUsername()).thenReturn("testuser");

        Boolean result = jwtHelper.validateToken(token, userDetails);

        assertThat(result).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        String expiredToken = createExpiredToken();
        when(userDetails.getUsername()).thenReturn("testuser");

        Boolean result = jwtHelper.validateToken(expiredToken, userDetails);

        assertThat(result).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidUsername() {
        String token = createValidToken();
        when(userDetails.getUsername()).thenReturn("differentuser");

        Boolean result = jwtHelper.validateToken(token, userDetails);

        assertThat(result).isFalse();
    }

    @Test
    void extractLogin_shouldReturnUsernameFromToken() {
        String token = createValidToken();

        String result = jwtHelper.extractLogin(token);

        assertThat(result).isEqualTo("testuser");
    }

    @Test
    void extractLogin_shouldReturnNullForInvalidToken() {
        String invalidToken = "invalid.token.here";

        String result = jwtHelper.extractLogin(invalidToken);

        assertThat(result).isNull();
    }

    @Test
    void extractExpiration_shouldReturnDate() {
        String token = createValidToken();

        Date result = jwtHelper.extractExpiration(token);

        assertThat(result).isAfter(new Date(System.currentTimeMillis() - 1000));
    }

    @Test
    void extractExpiration_shouldReturnNullForInvalidToken() {
        String invalidToken = "invalid.token.here";

        Date result = jwtHelper.extractExpiration(invalidToken);

        assertThat(result).isNull();
    }

    private String createValidToken() {
        return Jwts.builder()
                .claim("login", "testuser")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000000))
                .signWith(jwtHelper.generateKey())
                .compact();
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .claim("login", "testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000000))
                .signWith(jwtHelper.generateKey())
                .compact();
    }
}