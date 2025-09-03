package ru.practicum.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для случаев, когда сущность не найдена
 */
public class NotFoundException extends BaseException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "Ресурс не найден");
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, "Ресурс не найден", cause);
    }

    /**
     * @param resource Ресурс
     * @param id Идентификатор ресурса
     */
    public NotFoundException(String resource, String id) {
        super(String.format("%s с ID %s не найден", resource, id),
                HttpStatus.NOT_FOUND, "Ресурс не найден");
    }
}