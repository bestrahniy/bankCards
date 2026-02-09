package com.example.bankcards.exception.abstractClass;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;

    protected ApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
