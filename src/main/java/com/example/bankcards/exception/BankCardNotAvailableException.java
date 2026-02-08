package com.example.bankcards.exception;

public class BankCardNotAvailableException extends RuntimeException {

    public BankCardNotAvailableException(String massage) {
        super(massage);
    }

}
