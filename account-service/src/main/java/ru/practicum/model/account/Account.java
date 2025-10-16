package ru.practicum.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Счет
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Код валюты
     */
    private String currencyCode;

    /**
     * Баланс
     */
    private BigDecimal balance;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    private LocalDateTime updatedAt;
}
