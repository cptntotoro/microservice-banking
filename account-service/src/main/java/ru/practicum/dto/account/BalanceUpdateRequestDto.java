package ru.practicum.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateRequestDto {
    /**
     * Идентификатор аккаунта
     */
    private UUID userId;

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