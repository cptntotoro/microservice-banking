package ru.practicum.model.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Перевод средств между своими счетами
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnTransfer {
    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Идентификатор исходного счета
     */
    private UUID fromAccountId;

    /**
     * Идентификатор целевого счета
     */
    private UUID toAccountId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Код исходной валюты
     */
    private String fromCurrency;

    /**
     * Код целевой валюты
     */
    private String toCurrency;
}