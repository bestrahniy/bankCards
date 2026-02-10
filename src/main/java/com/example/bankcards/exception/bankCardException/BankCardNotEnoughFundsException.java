package com.example.bankcards.exception.bankCardException;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.ConflictException;

public class BankCardNotEnoughFundsException extends ConflictException {

    public BankCardNotEnoughFundsException() {
        super("lack of founds on the debit card", HttpStatus.CONFLICT);
    }

}
