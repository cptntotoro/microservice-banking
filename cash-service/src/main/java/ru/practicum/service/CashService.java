package ru.practicum.service;

import reactor.core.publisher.Mono;
import ru.practicum.model.CashRequest;
import ru.practicum.model.CashResponse;

/**
 * Сервис внесения и снятия денег
 */
public interface CashService {
    /**
     * Пололнить счет
     *
     * @param request Запрос на пополнение счета
     * @return Ответ
     */
    Mono<CashResponse> deposit(CashRequest request);

    /**
     * Снять средства со счета
     *
     * @param request Запрос на снятие средств
     * @return Ответ
     */
    Mono<CashResponse> withdraw(CashRequest request);
}
