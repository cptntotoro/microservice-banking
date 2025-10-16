package ru.practicum.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Запрос на пополнение счета
 */
@Data
@Builder
public class CashRequest {
    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Код валюты
     */
    private String currency;
}