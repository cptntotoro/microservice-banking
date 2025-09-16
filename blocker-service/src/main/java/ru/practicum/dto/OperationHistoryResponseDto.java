package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO ответа с историей операций
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationHistoryResponseDto {
    /**
     * Идентификатор операции
     */
    private UUID operationId;

    /**
     * Тип операции
     */
    private String operationType;

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