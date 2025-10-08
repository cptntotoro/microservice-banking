package ru.practicum.model.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Результат перевода
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResult {
    /**
     * Идентификатор перевода
     */
    private UUID transferId;

    /**
     * Идентификатор счета отправителя
     */
    private UUID fromAccountId;

    /**
     * Идентификатор счета получателя
     */
    private UUID toAccountId;

    /**
     * Код целевой валюты
     */
    private String toCurrency;

    /**
     * Email получателя
     */
    private String recipientEmail;

    /**
     * Сумма перевода
     */
    private BigDecimal amount;

    /**
     * Конвертированная сумма перевода
     */
    private BigDecimal convertedAmount;

    /**
     * Статус операции
     */
    private TransferStatus status;

    /**
     * Дата и время выполнения
     */
    private LocalDateTime createdAt;

    /**
     * Сообщение
     */
    private String message;

    /**
     * Ошибка
     */
    private TransferErrorCode errorCode;
}
