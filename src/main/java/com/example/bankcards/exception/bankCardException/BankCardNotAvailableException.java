package com.example.bankcards.exception.bankCardException;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.ConflictException;

public class BankCardNotAvailableException extends ConflictException {

    public BankCardNotAvailableException(String massage) {
        super(massage, HttpStatus.CONFLICT);
    }

}
