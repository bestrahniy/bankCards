package com.example.bankcards.exception.userException;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String login) {
        super(String.format("User with login: %s did not found", login),
            HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(UUID userId) {
        super(String.format("user with id: %s did not found", userId.toString()),
            HttpStatus.NOT_FOUND);
    }

}
