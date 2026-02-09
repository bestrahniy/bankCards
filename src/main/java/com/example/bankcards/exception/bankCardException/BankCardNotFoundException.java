package com.example.bankcards.exception.bankCardException;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.NotFoundException;

public class BankCardNotFoundException extends NotFoundException {

    public BankCardNotFoundException(String hashNumberCard) {
        super(String.format("Bank card with number: %s is not found", hashNumberCard),
            HttpStatus.NOT_FOUND);
    }

}
