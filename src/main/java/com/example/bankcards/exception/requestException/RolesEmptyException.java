package com.example.bankcards.exception.requestException;

import org.springframework.http.HttpStatus;

import com.example.bankcards.exception.abstractClass.BadRequestException;

public class RolesEmptyException extends BadRequestException {

    public RolesEmptyException() {
        super("this user has not role", HttpStatus.BAD_REQUEST);
    }
}
