package ru.practicum.service.transfer;

import reactor.core.publisher.Mono;
import ru.practicum.model.transfer.OtherTransfer;
import ru.practicum.model.transfer.OwnTransfer;
import ru.practicum.model.transfer.TransferResult;

/**
 * Сервис перевода средств
 */
public interface TransferService {
    /**
     * Перевести средства на свой счет
     *
     * @param ownTransfer Перевод средств на свой счет
     * @return Результат перевода
     */
    Mono<TransferResult> performOwnTransfer(OwnTransfer ownTransfer);

    /**
     * Перевести средства на счет другого пользователя
     *
     * @param otherTransfer Перевод средств на счет другого пользователя
     * @return Результат перевода
     */
    Mono<TransferResult> performOtherTransfer(OtherTransfer otherTransfer);
}