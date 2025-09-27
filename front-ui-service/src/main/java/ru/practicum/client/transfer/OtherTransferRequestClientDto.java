package ru.practicum.client.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Клиентский DTO перевода другому пользователю
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherTransferRequestClientDto {
    /**
     * Идентификатор пользователя
     */
    private UUID fromUserId;

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
}