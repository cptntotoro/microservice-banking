package ru.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Базовое исключение для REST API
 */
@Getter
public class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final String reason;

    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.reason = status.getReasonPhrase();
    }

    public BaseException(String message, HttpStatus status, String reason) {
        super(message);
        this.status = status;
        this.reason = reason;
    }
}