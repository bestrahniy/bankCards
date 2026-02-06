package com.example.bankcards.exception;

public class RolesEmptyException extends RuntimeException {

    public RolesEmptyException() {
        super("this user has not role");
    }
}
