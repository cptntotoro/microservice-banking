package ru.practicum.dto.cash;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO запроса на операции с наличными (внесение/снятие)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositWithdrawCashRequestDto {
    /**
     * Сумма операции
     */
    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    /**
     * Код валюты
     */
    @NotBlank(message = "Валюта обязательна")
    private String currency;

    @NotBlank(message = "Операция обязательна")
    private String operation;
}
