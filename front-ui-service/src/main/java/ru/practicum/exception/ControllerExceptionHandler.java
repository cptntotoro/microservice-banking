package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.practicum.exception.transfer.TransferException;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
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

    @ExceptionHandler(TransferException.class)
    public Mono<String> handleTransferException(TransferException e, Model model) {
        log.error("TransferException occurred: {}", e.getMessage(), e);

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.toString(),
                e.getReason(),
                String.format("Ошибка перевода: %s (код: %s)", e.getMessage(), e.getErrorCode()),
                LocalDateTime.now()
        );

        model.addAttribute("error", error);
        model.addAttribute("transferError", true);
        return Mono.just("page/dashboard"); // Возвращаем на dashboard с ошибкой
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<String> handleValidationException(ValidationException e, Model model) {
        log.warn("ValidationException occurred: {}", e.getMessage(), e);

        ApiError error = new ApiError(
                e.getStatus().toString(),
                e.getReason(),
                e.getMessage(),
                LocalDateTime.now()
        );

        model.addAttribute("error", error);
        model.addAttribute("validationError", true); // Опциональный флаг для фронтенда
        return Mono.just("page/dashboard"); // Возвращаем на dashboard для отображения ошибки в разметке
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<String> handleServiceUnavailable(ServiceUnavailableException e, Model model) {
        log.error("ServiceUnavailableException occurred: {} (service: {})",
                e.getMessage(), e.getServiceId(), e);

        ApiError error = new ApiError(
                e.getStatus().toString(),
                e.getReason(),
                String.format("%s. Сервис: %s. Проблема: %s",
                        e.getMessage(), e.getServiceId(), e.getServiceIssue()),
                LocalDateTime.now()
        );

        model.addAttribute("error", error);
        model.addAttribute("serviceUnavailable", true);
        model.addAttribute("serviceId", e.getServiceId());
        return Mono.just("util/service-unavailable");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<String> handleResponseStatus(ResponseStatusException ex, Model model) {
        log.error("ResponseStatusException occurred: {}", ex.getMessage(), ex);

        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            model.addAttribute("page", "util/not-found");
            return Mono.just("layout");
        }
        return Mono.error(ex);
    }

    @ExceptionHandler(Exception.class)
    public Mono<String> handleGenericException(Exception e, Model model) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка. Попробуйте позже.",
                LocalDateTime.now()
        );

        model.addAttribute("error", error);
        return Mono.just("util/error");
    }
}
