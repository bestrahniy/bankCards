package com.example.bankcards.exception.jwtException;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.ConflictException;

public class TokenNotActiveException extends ConflictException {

    public TokenNotActiveException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}
