package com.example.bankcards.exception;

public class TransactionStatusNotFoundException extends RuntimeException {

    public TransactionStatusNotFoundException(String message) {
        super(message);
    }

}
