package ru.practicum.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для операций с переводами
 */
public class TransferException extends BaseException {

    public TransferException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public TransferException(String message, HttpStatus status) {
        super(message, status);
    }
}
