package com.example.bankcards.exception.abstractClass;

import org.springframework.http.HttpStatus;

public abstract class NotFoundException extends ApiException {

    protected NotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

}