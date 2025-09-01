package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Mono<String> handleBaseException(BaseException e, Model model) {
        ApiError error = new ApiError(
                e.getStatus().toString(),
                e.getReason(),
                e.getMessage(),
                LocalDateTime.now()
        );

        model.addAttribute("error", error);
        return Mono.just("util/error");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<String> handleResponseStatus(ResponseStatusException ex, Model model) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            model.addAttribute("page", "util/not-found");
            return Mono.just("layout");
        }
        return Mono.error(ex);
    }
}
