package ru.practicum.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для ответа с данными счета
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {
    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Код валюты
     */
    private String currencyCode;

    /**
     * Баланс счета
     */
    private BigDecimal balance;
}
