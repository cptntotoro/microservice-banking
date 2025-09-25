package ru.practicum.client.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO запроса клиента на операции с наичными
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