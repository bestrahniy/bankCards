package com.example.bankcards.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String login) {
        super(String.format("User with login: %s did not found", login));
    }

    public UserNotFoundException(UUID userId) {
        super(String.format("user with id: %s did not found", userId.toString()));
    }

}
