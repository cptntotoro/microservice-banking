package ru.practicum.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.OperationCheckRequest;
import ru.practicum.model.OperationCheckResponse;
import ru.practicum.model.OperationHistory;

import java.util.UUID;

/**
 * Сервис проверки операций
 */
public interface BlockerService {
    /**
     * Проверить операцию
     *
     * @param request Запрос на проверку операции
     * @return Результат проверки операции
     */
    Mono<OperationCheckResponse> checkOperation(OperationCheckRequest request);

    /**
     * Получить историю операций пользователя
     *
     * @param userId Идентификатор пользователя
     * @return Список операций пользователя
     */
    Flux<OperationHistory> getOperationHistory(UUID userId);
}

