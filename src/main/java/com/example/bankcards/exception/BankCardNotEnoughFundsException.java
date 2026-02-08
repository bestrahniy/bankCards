package com.example.bankcards.exception;

public class BankCardNotEnoughFundsException extends RuntimeException {

    public BankCardNotEnoughFundsException() {
        super("lack of founds on the debit card");
    }

}
