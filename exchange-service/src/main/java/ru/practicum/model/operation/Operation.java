package ru.practicum.model.operation;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Валютная операция
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Исходная валюта
     */
    private String fromCurrency;

    /**
     * Целевая валюта
     */
    private String toCurrency;

    /**
     * Сумма исходной валюты
     */
    private BigDecimal amount;

    /**
     * Сумма целевой валюты
     */
    private BigDecimal convertedAmount;

    /**
     * Курс обмена
     */
    private BigDecimal exchangeRate;

    /**
     * Тип операции
     */
    private OperationType operationType;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}