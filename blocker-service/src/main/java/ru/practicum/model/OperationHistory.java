package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель истории операций
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationHistory {
    /**
     * Идентификатор операции
     */
    private UUID operationId;

    /**
     * Тип операции
     */
    private OperationType operationType;

    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Валюта операции
     */
    private String currency;

    /**
     * Время операции
     */
    private LocalDateTime timestamp;

    /**
     * Флаг блокировки
     */
    private Boolean blocked;

    /**
     * Код причины блокировки
     */
    private String blockReasonCode;

    /**
     * Оценка риска
     */
    private Integer riskScore;
}