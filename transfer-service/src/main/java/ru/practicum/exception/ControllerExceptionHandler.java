package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ServiceClientException.class)
    public Mono<ResponseEntity<ApiError>> handleServiceClientException(ServiceClientException ex) {
        log.warn("Ошибка внешнего сервиса: {} - {} (operation: {})",
                ex.getServiceName(), ex.getMessage(), ex.getOperation());

        ApiError error = new ApiError(
                ex.getStatus().toString(),
                ex.getReason(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return Mono.just(ResponseEntity.status(ex.getStatus()).body(error));
    }

    @ExceptionHandler(BaseException.class)
    public Mono<ResponseEntity<ApiError>> handleBaseException(BaseException ex) {
        log.warn("Business error: {} - {}", ex.getStatus(), ex.getMessage());

        ApiError error = new ApiError(
                ex.getStatus().toString(),
                ex.getReason(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return Mono.just(ResponseEntity.status(ex.getStatus()).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleValidationException(WebExchangeBindException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        String errorMessage = ex.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Неверные данные в запросе");

        ValidationException validationEx = new ValidationException(errorMessage);

        ApiError error = new ApiError(
                validationEx.getStatus().toString(),
                validationEx.getReason(),
                validationEx.getMessage(),
                LocalDateTime.now()
        );

        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleAllExceptions(Exception ex) {
        log.error("Internal server error: ", ex);

        BaseException internalError = new BaseException(
                "Внутренняя ошибка сервера",
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
        );

        ApiError error = new ApiError(
                internalError.getStatus().toString(),
                internalError.getReason(),
                internalError.getMessage(),
                LocalDateTime.now()
        );

        return Mono.just(ResponseEntity.internalServerError().body(error));
    }
}