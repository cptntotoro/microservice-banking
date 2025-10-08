package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Глобальный обработчик исключений для REST API
 */
@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    /**
     * Обработка BaseException
     */
    @ExceptionHandler(BaseException.class)
    public Mono<ResponseEntity<ApiError>> handleBaseException(BaseException e) {
        log.warn("Base exception: {} - {}", e.getStatus(), e.getMessage());

        ApiError error = new ApiError(
                e.getStatus().toString(),
                e.getReason(),
                e.getMessage(),
                LocalDateTime.now(),
                e instanceof ValidationException ? ((ValidationException) e).getErrorCode() : null
        );

        return Mono.just(ResponseEntity.status(e.getStatus()).body(error));
    }

    /**
     * Обработка ValidationException
     */
    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ApiError>> handleValidationException(ValidationException e) {
        log.warn("Validation exception: {} - {} (code: {})", e.getStatus(), e.getMessage(), e.getErrorCode());

        ApiError error = new ApiError(
                e.getStatus().toString(),
                e.getReason(),
                e.getMessage(),
                LocalDateTime.now(),
                e.getErrorCode()
        );

        return Mono.just(ResponseEntity.status(e.getStatus()).body(error));
    }

    /**
     * Обработка ошибок валидации Spring WebFlux
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleWebExchangeBindException(WebExchangeBindException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.toString(),
                ErrorReasons.VALIDATION_ERROR,
                "Неверные данные в запросе",
                LocalDateTime.now(),
                errors,
                ErrorReasons.VALIDATION_ERROR
        );

        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    /**
     * Обработка ошибок валидации с FieldError
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ApiError>> handleServerWebInputException(ServerWebInputException ex) {
        log.warn("Input error: {}", ex.getMessage());

        List<ApiError.FieldError> fieldErrors = List.of(new ApiError.FieldError(
                "request",
                "Неверный формат данных",
                null
        ));

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.toString(),
                "Ошибка ввода",
                ex.getReason(),
                LocalDateTime.now(),
                fieldErrors,
                ErrorReasons.VALIDATION_ERROR
        );

        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleAllExceptions(Exception ex) {
        log.error("Internal server error: ", ex);

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                ErrorReasons.INTERNAL_ERROR,
                "Произошла непредвиденная ошибка",
                LocalDateTime.now(),
                ErrorReasons.INTERNAL_ERROR
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}