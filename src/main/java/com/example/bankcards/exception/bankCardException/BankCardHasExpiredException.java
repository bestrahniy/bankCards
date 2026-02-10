package com.example.bankcards.exception.bankCardException;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.ConflictException;

public class BankCardHasExpiredException extends ConflictException {

    public BankCardHasExpiredException(String hashNumberCard) {
        super(String.format("Bank card with number: %s has expired", hashNumberCard),
            HttpStatus.CONFLICT);
    }

}
