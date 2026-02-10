package com.example.bankcards.config;

import java.time.LocalDateTime;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.example.bankcards.dto.response.ErrorResponse;
import com.example.bankcards.exception.abstractClass.ConflictException;
import com.example.bankcards.exception.abstractClass.NotFoundException;
import com.example.bankcards.exception.abstractClass.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the application
 * Catches specific exceptions and returns standardized error responses
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles BadRequestException and returns a 400 error response
     * @param exception The caught BadRequestException
     * @return ErrorResponse with details
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException exception) {
        log.warn("Bad request: {}", exception.getMessage());
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(exception.getMessage())
                .build();
    }

    /**
     * Handles NotFoundException and returns a 404 error response
     * @param exception The caught NotFoundException.
     * @return ErrorResponse with details.
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException exception) {
        log.warn("Not found: {}", exception.getMessage());
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(exception.getMessage())
                .build();
    }

    /**
     * Handles ConflictException and returns a 409 error response.
     * @param exception The caught ConflictException.
     * @return ErrorResponse with details.
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException exception) {
        log.warn("Conflict: {}", exception.getMessage());
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(exception.getMessage())
                .build();
    }

    /**
     * Handles UnauthorizedException and returns a 401 error response.
     * @param exception The caught UnauthorizedException.
     * @return ErrorResponse with details.
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(UnauthorizedException exception) {
        log.warn("Unauthorized: {}", exception.getMessage());
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(exception.getMessage())
                .build();
    }

}
