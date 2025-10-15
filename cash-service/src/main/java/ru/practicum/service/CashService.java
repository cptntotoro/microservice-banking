package ru.practicum.service;

import reactor.core.publisher.Mono;
import ru.practicum.dto.CashRequestDto;
import ru.practicum.model.CashRequest;
import ru.practicum.model.CashResponse;

/**
 * Сервис внесения и снятия денег
 */
public interface CashService {
    Mono<CashResponse> cashOperation(CashRequestDto request);
}
