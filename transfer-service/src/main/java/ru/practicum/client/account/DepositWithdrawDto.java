package ru.practicum.client.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @NotNull(message = "ID счета обязательно")
    private UUID accountId;

    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}