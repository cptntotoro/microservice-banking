package ru.practicum.service;

import reactor.core.publisher.Mono;
import ru.practicum.dto.CashRequestDto;
import ru.practicum.model.CashResponse;

/**
 * Сервис внесения и снятия денег
 */
public interface CashService {
    /**
     * Произвести валютную операцию
     *
     * @param request DTO запроса на пополнение счета
     * @return Ответ на запрос на пополнение счета
     */
    Mono<CashResponse> cashOperation(CashRequestDto request);
}
