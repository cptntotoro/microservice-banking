package ru.practicum.dto.exchange;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvertCurrencyRequestDto {
    /**
     * Код исходной валюты
     */
    @NotNull(message = "Исходная валюта обязательна")
    private String fromCurrency;

    /**
     * Код целевой валюты
     */
    @NotNull(message = "Целевая валюта обязательна")
    private String toCurrency;

    /**
     * Сумма операции
     */
    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}
