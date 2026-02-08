package com.example.bankcards.exception;

public class BankCardNotFoundException extends RuntimeException {

    public BankCardNotFoundException(String hashNumberCard) {
        super(String.format("Bank card with number: %s is not found", hashNumberCard));
    }
}
