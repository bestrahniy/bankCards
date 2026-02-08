package com.example.bankcards.exception;

public class TransactionTypeNotFoundException extends RuntimeException {

    public TransactionTypeNotFoundException(String message) {
        super(message);
    }

}
