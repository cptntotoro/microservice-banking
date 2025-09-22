package ru.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Исключение недоступного сервиса
 */
@Getter
public class ServiceUnavailableException extends BaseException {
    /**
     * Идентификатор недоступного сервиса
     */
    private final String serviceId;

    /**
     * Детальное описание проблемы с сервисом.
     * Например: "Сервис аккаунтов не отвечает на запросы".
     */
    private final String serviceIssue;

    public ServiceUnavailableException(String message, String serviceId, String serviceIssue) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "Сервис недоступен");
        this.serviceId = serviceId;
        this.serviceIssue = serviceIssue;
    }

    /**
     * Конструктор с сообщением об ошибке, статусом HTTP, причиной и идентификатором сервиса.
     *
     * @param message текст ошибки для пользователя
     * @param status HTTP статус ответа
     * @param reason причина ошибки (краткое описание)
     * @param serviceId идентификатор недоступного сервиса
     * @param serviceIssue детальное описание проблемы с сервисом
     */
    public ServiceUnavailableException(String message, HttpStatus status, String reason,
                                       String serviceId, String serviceIssue) {
        super(message, status, reason);
        this.serviceId = serviceId;
        this.serviceIssue = serviceIssue;
    }

    /**
     * Конструктор с сообщением об ошибке и идентификатором сервиса.
     * Использует стандартное описание проблемы.
     *
     * @param message текст ошибки для пользователя
     * @param serviceId идентификатор недоступного сервиса
     */
    public ServiceUnavailableException(String message, String serviceId) {
        this(message, serviceId, "Сервис временно недоступен");
    }

    /**
     * Получение детальной информации об ошибке в формате строки.
     *
     * @return строка с описанием ошибки, включая идентификатор сервиса и проблему
     */
    @Override
    public String getMessage() {
        return super.getMessage() + String.format(" (сервис: %s, проблема: %s)",
                serviceId, serviceIssue);
    }
}
