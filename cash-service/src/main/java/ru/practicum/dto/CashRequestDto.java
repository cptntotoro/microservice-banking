package ru.practicum.dto;

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
public class CashRequestDto {
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