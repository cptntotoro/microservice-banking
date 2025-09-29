package ru.practicum.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для ошибок валидации пользовательского ввода
 */
public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "Ошибка валидации");
    }

    public ValidationException(String message, String reason) {
        super(message, HttpStatus.BAD_REQUEST, reason);
    }
}