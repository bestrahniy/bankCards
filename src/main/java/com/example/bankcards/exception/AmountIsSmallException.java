package com.example.bankcards.exception;

public class AmountIsSmallException extends RuntimeException {

    public AmountIsSmallException(String message) {
        super(message);
    }

}
