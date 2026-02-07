package com.example.bankcards.exception;

public class RequestIsNullException extends RuntimeException {

    public RequestIsNullException(Object object) {
        super(String.format("request: %s is null", object.getClass()));
    }
}
