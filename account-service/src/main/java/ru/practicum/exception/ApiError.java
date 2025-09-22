package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Ошибка REST API
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String status,
        String reason,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime timestamp,
        Map<String, String> errors,
        List<FieldError> fieldErrors,
        String errorCode
) {

    public ApiError(String status, String reason, String message, LocalDateTime timestamp, String errorCode) {
        this(status, reason, message, timestamp, null, null, errorCode);
    }

    public ApiError(String status, String reason, String message, LocalDateTime timestamp, Map<String, String> errors, String errorCode) {
        this(status, reason, message, timestamp, errors, null, errorCode);
    }

    public ApiError(String status, String reason, String message, LocalDateTime timestamp, List<FieldError> fieldErrors, String errorCode) {
        this(status, reason, message, timestamp, null, fieldErrors, errorCode);
    }

    /**
     * Ошибка валидации поля
     */
    public record FieldError(String field, String message, Object rejectedValue) {
        public FieldError(String field, String message) {
            this(field, message, null);
        }
    }
}