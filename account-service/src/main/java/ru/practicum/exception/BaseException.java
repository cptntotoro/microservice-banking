package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * Базовое исключение для REST API
 */
@Getter
public class BaseException extends RuntimeException {

    private final HttpStatus status;
    private final String reason;

    public BaseException(String message, HttpStatus status, String reason) {
        super(message);
        this.status = status;
        this.reason = reason;
    }

    public BaseException(String message, HttpStatus status) {
        this(message, status, status.getReasonPhrase());
    }

    public BaseException(String message, HttpStatus status, String reason, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.reason = reason;
    }
}