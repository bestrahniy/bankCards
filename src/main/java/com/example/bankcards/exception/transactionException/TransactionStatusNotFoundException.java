package com.example.bankcards.exception.transactionException;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.NotFoundException;

public class TransactionStatusNotFoundException extends NotFoundException {

    public TransactionStatusNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

}
