package ru.practicum.service.cash;

import reactor.core.publisher.Mono;
import ru.practicum.dto.cash.DepositWithdrawCashRequestDto;
import ru.practicum.model.cash.Cash;

import java.util.UUID;

/**
 * Сервис обналичивания денег
 */
public interface CashService {

    /**
     * Выполнить операцию с наличными
     *
     * @param userId Идентификатор пользователя
     * @param requestDto DTO запроса на операции с наличными (внесение/снятие)
     * @return Операция с наличными
     */
    Mono<Cash> cashOperation(UUID userId, DepositWithdrawCashRequestDto requestDto);
}