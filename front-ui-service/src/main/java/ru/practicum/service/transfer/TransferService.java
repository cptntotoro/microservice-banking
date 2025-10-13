package ru.practicum.service.transfer;

import reactor.core.publisher.Mono;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.model.transfer.OtherTransfer;
import ru.practicum.model.transfer.OwnTransfer;
import ru.practicum.model.transfer.TransferResult;

import java.util.UUID;

/**
 * Сервис перевода средств
 */
public interface TransferService {
    /**
     * Перевести средства между своими счетами
     *
     * @param ownTransfer Перевод средств между своими счетами
     * @return Результат перевода
     */
    Mono<TransferResult> performOwnTransfer(OwnTransferRequestDto requestDto, UUID userId);

    /**
     * Перевести средства на счет другого пользователя
     *
     * @param otherTransfer Перевод средств на счет другого пользователя
     * @return Результат перевода
     */
    Mono<TransferResult> performOtherTransfer(OtherTransfer otherTransfer);
}