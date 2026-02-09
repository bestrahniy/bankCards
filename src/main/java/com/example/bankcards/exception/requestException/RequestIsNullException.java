package com.example.bankcards.exception.requestException;

import org.springframework.http.HttpStatus;

import com.example.bankcards.exception.abstractClass.BadRequestException;

public class RequestIsNullException extends BadRequestException {

    public RequestIsNullException(Object object) {
        super(String.format("request: %s is null", object.getClass()), HttpStatus.BAD_REQUEST);
    }
}
