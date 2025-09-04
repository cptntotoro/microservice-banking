package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * Исключение для ошибок валидации с указанием HTTP статуса
 */
@Getter
public class ValidationException extends BaseException {

    private final String errorCode;

    /**
     * Конструктор с сообщением и статусом
     */
    public ValidationException(String message, HttpStatus status) {
        super(message, status);
        this.errorCode = "VALIDATION_ERROR";
    }

    /**
     * Конструктор с сообщением, статусом и кодом ошибки
     */
    public ValidationException(String message, HttpStatus status, String errorCode) {
        super(message, status);
        this.errorCode = errorCode;
    }

    /**
     * Конструктор с сообщением (по умолчанию BAD_REQUEST)
     */
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
        this.errorCode = "VALIDATION_ERROR";
    }

    /**
     * Конструктор с сообщением и причиной
     */
    public ValidationException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, String.valueOf(cause));
        this.errorCode = "VALIDATION_ERROR";
    }

    /**
     * Конструктор с сообщением, причиной и статусом
     */
    public ValidationException(String message, Throwable cause, HttpStatus status) {
        super(message, status, String.valueOf(cause));
        this.errorCode = "VALIDATION_ERROR";
    }
}