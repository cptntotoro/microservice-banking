package ru.practicum.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение недоступного сервиса
 */
public class ServiceUnavailableException extends BaseException {
    public ServiceUnavailableException(String serviceName) {
        super("Сервис недоступен: " + serviceName, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String serviceName, String message) {
        super("Сервис недоступен: " + serviceName + ". " + message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}