package ru.practicum.service.cash;

import reactor.core.publisher.Mono;
import ru.practicum.client.cash.dto.CashRequestClientDto;
import ru.practicum.dto.cash.DepositWithdrawCashRequestDto;
import ru.practicum.model.cash.Cash;

import java.util.UUID;

/**
 * Сервис обналичивания денег
 */
public interface CashService {

    /**
     * Операция над счетом
     *
     * @return Операция с наличными
     */
    Mono<Cash> cashOperation(UUID userId, DepositWithdrawCashRequestDto requestDto);
}