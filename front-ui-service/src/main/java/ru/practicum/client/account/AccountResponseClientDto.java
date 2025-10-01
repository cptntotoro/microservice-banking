package ru.practicum.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Клиентский DTO ответа с данными о счете пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseClientDto {
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

    /**
     * Дата создания счета
     */
    private LocalDateTime createdAt;
}
