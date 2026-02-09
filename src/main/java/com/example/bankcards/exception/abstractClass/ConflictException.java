package com.example.bankcards.exception.abstractClass;

import org.springframework.http.HttpStatus;

public abstract class ConflictException extends ApiException {

    protected ConflictException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

}