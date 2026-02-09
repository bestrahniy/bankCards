package com.example.bankcards.exception.transactionException;

import org.springframework.http.HttpStatus;

import com.example.bankcards.exception.abstractClass.NotFoundException;

public class TransactionTypeNotFoundException extends NotFoundException {

    public TransactionTypeNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

}
