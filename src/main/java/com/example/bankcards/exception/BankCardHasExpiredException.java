package com.example.bankcards.exception;

public class BankCardHasExpiredException extends RuntimeException {

    public BankCardHasExpiredException(String hashNumberCard) {
        super(String.format("Bank card with number: %s has expired", hashNumberCard));
    }

}
