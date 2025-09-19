package ru.practicum.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение валидации
 */
public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}