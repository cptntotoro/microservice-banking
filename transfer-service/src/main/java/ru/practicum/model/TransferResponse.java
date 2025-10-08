package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Результат перевода средств
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {
    /**
     * Статус операции перевода
     */
    private OperationStatus status;

    /**
     * Идентификатор счета отправителя
     */
    private UUID fromAccountId;

    /**
     * Идентификатор счета получателя
     */
    private UUID toAccountId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Сумма целевой операции
     */
    private BigDecimal convertedAmount;
}