package ru.practicum.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для пополнения/снятия
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositWithdrawDto {
    /**
     * Идентификатор счета
     */
    @NotNull(message = "ID счета обязательно")
    private UUID accountId;

    /**
     * Сумма операции
     */
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}