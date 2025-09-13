package ru.practicum.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Запрос на пополнение счета
 */
@Data
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
     * Сумма
     */
    private BigDecimal amount;

    /**
     * Валюта
     */
    private String currency;
}