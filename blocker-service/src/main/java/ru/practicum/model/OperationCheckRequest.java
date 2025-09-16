package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Запрос на проверку операции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationCheckRequest {
    /**
     * Уникальный идентификатор операции
     */
    private UUID operationId;

    /**
     * Тип операции
     */
    private OperationType operationType;

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Валюта операции (3 символа)
     */
    private String currency;

    /**
     * Время операции
     */
    private LocalDateTime timestamp;
}