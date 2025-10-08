package ru.practicum.client.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO запроса на пополнение или снятие средств со счета
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateRequestDto {
    /**
     * Идентификатор аккаунта
     */
    private UUID accountId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Является ли пополнением
     */
    private boolean isDeposit;
}