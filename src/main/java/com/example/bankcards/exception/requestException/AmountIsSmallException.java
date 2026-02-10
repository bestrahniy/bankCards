package com.example.bankcards.exception.requestException;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.BadRequestException;

public class AmountIsSmallException extends BadRequestException {

    public AmountIsSmallException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}
