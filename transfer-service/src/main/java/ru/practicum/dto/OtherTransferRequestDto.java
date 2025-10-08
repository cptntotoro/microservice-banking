package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO запроса на перевод другому человеку
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherTransferRequestDto {
    /**
     * Идентификатор пользователя
     */
    private UUID fromUserId;

    /**
     * Идентификатор счета отправителя
     */
    private UUID fromAccountId;

    /**
     * Электронная почта получателя
     */
    private String recipientEmail;

    /**
     * Валюта счета получателя
     */
    private String toCurrency;

    /**
     * Сумма операции
     */
    private BigDecimal amount;
}