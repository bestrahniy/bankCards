package com.example.bankcards.exception.jwtException;

import org.springframework.http.HttpStatus;

import com.example.bankcards.exception.abstractClass.UnauthorizedException;

public class TokenHasExpiredException extends UnauthorizedException {

    public TokenHasExpiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

}
