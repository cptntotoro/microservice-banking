package ru.practicum.service.cash;

import reactor.core.publisher.Mono;
import ru.practicum.model.cash.Cash;

/**
 * Сервис обналичивания денег
 */
public interface CashService {

    /**
     * Пополнить свой счета
     *
     * @param cash Операция с наличными
     * @return Операция с наличными
     */
    Mono<Cash> deposit(Cash cash);

    /**
     * Снять средства со своего счета
     *
     * @param cash Операция с наличными
     * @return Операция с наличными
     */
    Mono<Cash> withdraw(Cash cash);
}