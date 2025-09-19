package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ApiError(
        String status,
        String reason,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime timestamp
) {
    public ApiError(BaseException ex) {
        this(
                ex.getStatus().toString(),
                ex.getReason(),
                ex.getMessage(),
                LocalDateTime.now()
        );
    }
}