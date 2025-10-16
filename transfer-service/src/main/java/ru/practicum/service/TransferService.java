package ru.practicum.service;

import reactor.core.publisher.Mono;
import ru.practicum.dto.OtherTransferRequestDto;
import ru.practicum.dto.OwnTransferRequestDto;
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
    Mono<TransferResponse> transferBetweenOwnAccounts(OwnTransferRequestDto request);

    /**
     * Перевести средства на счет другого пользователя
     *
     * @return Результат перевода средств
     */
    Mono<TransferResponse> transferToOtherAccount(OtherTransferRequestDto requestDto);
}
