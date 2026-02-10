package com.example.bankcards.exception.jwtException;

import org.springframework.http.HttpStatus;

import com.example.bankcards.exception.abstractClass.NotFoundException;

public class RefreshTokenNotFoundException extends NotFoundException {

    public RefreshTokenNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

}
