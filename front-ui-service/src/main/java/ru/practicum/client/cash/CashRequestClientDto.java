package ru.practicum.client.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO запроса на пополнение счета
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRequestClientDto {
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
     * Валюта
     */
    private String currency;
}