package com.example.bankcards.exception;

public class UserEntityNullException extends RuntimeException {

    public UserEntityNullException() {
        super("User is null");
    }

}
