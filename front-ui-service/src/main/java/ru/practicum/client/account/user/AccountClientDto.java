package ru.practicum.client.account.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для представления счета пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountClientDto {

    /**
     * Идентификатор счета.
     */
    private UUID id;

    /**
     * Валюта счета.
     */
    private CurrencyClientDto currency;

    /**
     * Баланс счета.
     */
    private BigDecimal balance;
}
