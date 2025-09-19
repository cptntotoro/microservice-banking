package ru.practicum.client.exchange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для запроса конвертации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertRequestDto {

    @NotBlank(message = "Код валюты отправителя обязателен")
    private String fromCurrency;

    @NotBlank(message = "Код валюты получателя обязателен")
    private String toCurrency;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}