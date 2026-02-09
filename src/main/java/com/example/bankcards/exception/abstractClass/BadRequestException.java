package com.example.bankcards.exception.abstractClass;

import org.springframework.http.HttpStatus;

public abstract class BadRequestException extends ApiException {

    protected BadRequestException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

}
