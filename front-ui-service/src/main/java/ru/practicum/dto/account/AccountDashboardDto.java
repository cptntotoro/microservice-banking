package ru.practicum.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для отображения данных счета на дашборде
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDashboardDto {
    /**
     * Идентификатор счета
     */
    private UUID id;

    /**
     * Код валюты
     */
    private String currencyCode;

    /**
     * Баланс счета
     */
    private BigDecimal balance;
}