package ru.practicum.model.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Перевод средств на счет другого пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherTransfer {
    /**
     * Идентификатор пользователя-отправителя
     */
    private UUID userId;

    /**
     * Идентификатор счета отправителя
     */
    private UUID fromAccountId;

    /**
     * Код целевой валюты
     */
    private String toCurrency;

    /**
     * Email получателя
     */
    private String recipientEmail;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Код исходной валюты
     */
    private String fromCurrency;
}