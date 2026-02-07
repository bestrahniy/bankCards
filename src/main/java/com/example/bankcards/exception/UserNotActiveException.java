package com.example.bankcards.exception;

import java.util.UUID;

public class UserNotActiveException extends RuntimeException {

    public UserNotActiveException(UUID userId) {
        super(String.format("User with id: %s is not active", userId.toString()));
    }

}
