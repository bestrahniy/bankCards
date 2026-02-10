package com.example.bankcards.exception.encryprionExceprion;

import org.springframework.http.HttpStatus;
import com.example.bankcards.exception.abstractClass.UnauthorizedException;

public class EncryptionException extends UnauthorizedException {

    public EncryptionException(String massage) {
        super(massage, HttpStatus.UNAUTHORIZED);
    }

}
