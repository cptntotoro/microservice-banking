package ru.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceClientException extends BaseException {
    private final String serviceName;
    private final String operation;

    public ServiceClientException(String serviceName, String operation, String message, HttpStatus status) {
        super(message, status);
        this.serviceName = serviceName;
        this.operation = operation;
    }

    public ServiceClientException(String serviceName, String operation, String message, HttpStatus status, String reason) {
        super(message, status, reason);
        this.serviceName = serviceName;
        this.operation = operation;
    }

    // Статические фабричные методы
    public static ServiceClientException unavailable(String serviceName, String operation, String message) {
        return new ServiceClientException(serviceName, operation, message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public static ServiceClientException timeout(String serviceName, String operation, String message) {
        return new ServiceClientException(serviceName, operation, message, HttpStatus.REQUEST_TIMEOUT);
    }

    public static ServiceClientException internalError(String serviceName, String operation, String message) {
        return new ServiceClientException(serviceName, operation, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}