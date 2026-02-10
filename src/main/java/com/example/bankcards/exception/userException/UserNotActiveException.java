package com.example.bankcards.exception.userException;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.ConflictException;

public class UserNotActiveException extends ConflictException {

    public UserNotActiveException(UUID userId) {
        super(String.format("User with id: %s is not active", userId.toString()), HttpStatus.CONFLICT);
    }

    public UserNotActiveException(String login) {
        super(String.format("User with login: %s is not active", login), HttpStatus.CONFLICT);
    }

}
