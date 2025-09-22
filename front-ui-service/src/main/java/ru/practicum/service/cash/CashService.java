package ru.practicum.service.cash;

import reactor.core.publisher.Mono;
import ru.practicum.model.cash.Cash;

/**
 * Интерфейс сервиса обналичивания денег
 */
public interface CashService {

    /**
     * Пополнение счета.
     *
     * @param model модель запроса
     * @return Mono с моделью ответа
     */
    Mono<Cash> deposit(Cash model);

    /**
     * Снятие средств со счета.
     *
     * @param model модель запроса
     * @return Mono с моделью ответа
     */
    Mono<Cash> withdraw(Cash model);
}