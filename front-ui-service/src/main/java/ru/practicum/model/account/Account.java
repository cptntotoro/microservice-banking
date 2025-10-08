package ru.practicum.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Счет
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /**
     * Идентификатор
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

    /**
     * Дата создания счета
     */
    private LocalDateTime createdAt;
}