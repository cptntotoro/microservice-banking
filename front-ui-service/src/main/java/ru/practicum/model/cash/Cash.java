package ru.practicum.model.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Операция с наличными
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cash {
    /**
     * Идентификатор операции
     */
    private UUID operationId;

    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Тип операции
     */
    private String operationType;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Код валюты
     */
    private String currency;

    /**
     * Статус операции
     */
    private String status;

    /**
     * Сообщение
     */
    private String message;
}