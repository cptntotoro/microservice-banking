package ru.practicum.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение ненайденных ресурсов
 */
public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String resource, Object id) {
        super(String.format("%s с ID %s не найден", resource, id), HttpStatus.NOT_FOUND);
    }
}