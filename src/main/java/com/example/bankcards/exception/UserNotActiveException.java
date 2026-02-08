package com.example.bankcards.exception;

import java.util.UUID;

public class UserNotActiveException extends RuntimeException {

    public UserNotActiveException(UUID userId) {
        super(String.format("User with id: %s is not active", userId.toString()));
    }

    public UserNotActiveException(String login) {
        super(String.format("User with login: %s is not active", login));
    }

}
