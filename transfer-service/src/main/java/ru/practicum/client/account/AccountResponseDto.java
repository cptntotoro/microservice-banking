package ru.practicum.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
     * Идентификатор счета
     */
    private UUID id;

    /**
     * Идентификатор валюты
     */
    private UUID currencyId;

    /**
     * Код валюты
     */
    private String currencyCode;

    /**
     * Название валюты
     */
    private String currencyName;

    /**
     * Баланс счета
     */
    private BigDecimal balance;

    /**
     * Номер счета
     */
    private String accountNumber;

    /**
     * Дата создания счета
     */
    private LocalDateTime createdAt;
}
