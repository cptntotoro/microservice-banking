package ru.practicum.service;

import reactor.core.publisher.Mono;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;

/**
 * Сервис перевода средств между счетами
 */
public interface TransferService {
    /**
     * Перевести средства между своими счетами
     *
     * @param request Запрос на перевод средств
     * @return Результат перевода средств
     */
    Mono<TransferResponse> transferBetweenOwnAccounts(TransferRequest request);

    /**
     * Перевести средства на счет другого пользователя
     *
     * @param request Запрос на перевод средств
     * @return Результат перевода средств
     */
    Mono<TransferResponse> transferToOtherAccount(TransferRequest request);
}
