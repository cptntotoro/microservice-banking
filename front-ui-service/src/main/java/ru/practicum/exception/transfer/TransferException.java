package ru.practicum.exception.transfer;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;
import ru.practicum.model.transfer.TransferErrorCode;

/**
 * Исключение для операций с переводами
 */
@Getter
public class TransferException extends BaseException {

    private final TransferErrorCode errorCode;

    public TransferException(String message, TransferErrorCode errorCode) {
        super(message, HttpStatus.BAD_REQUEST, mapErrorCodeToReason(errorCode));
        this.errorCode = errorCode;
    }

    public TransferException(String message, HttpStatus status, String reason, TransferErrorCode errorCode) {
        super(message, status, reason);
        this.errorCode = errorCode;
    }

    private static String mapErrorCodeToReason(TransferErrorCode code) {
        return switch (code) {
            case ACCOUNT_OWNERSHIP_VIOLATION -> "Нарушение прав владения счетом";
            case SAME_ACCOUNT_TRANSFER -> "Перевод на тот же счет";
            case FROM_ACCOUNT_NOT_FOUND -> "Счет отправителя не найден";
            case TO_ACCOUNT_NOT_FOUND -> "Счет получателя не найден";
            case INSUFFICIENT_BALANCE -> "Недостаточно средств";
            case INVALID_AMOUNT -> "Некорректная сумма";
            case RECIPIENT_NOT_FOUND -> "Получатель не найден";
            case RECIPIENT_NO_ACCOUNT -> "У получателя нет счета в указанной валюте";
            case TRANSFER_SERVICE_UNAVAILABLE -> "Сервис переводов недоступен";
            case EXCHANGE_RATE_UNAVAILABLE -> "Курсы валют недоступны";
            case BLOCKER_REJECTION -> "Операция заблокирована";
            default -> "Внутренняя ошибка";
        };
    }
}