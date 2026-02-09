package com.example.bankcards.exception.abstractClass;

import org.springframework.http.HttpStatus;

public abstract class UnauthorizedException extends ApiException {

    protected UnauthorizedException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

}
