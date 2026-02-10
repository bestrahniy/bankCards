package com.example.bankcards.exception.userException;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.BadRequestException;

public class UserNullException extends BadRequestException {

    public UserNullException() {
        super("User is null",  HttpStatus.BAD_REQUEST);
    }

}
