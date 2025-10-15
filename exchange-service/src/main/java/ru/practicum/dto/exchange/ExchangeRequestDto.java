package ru.practicum.dto.exchange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.operation.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Запрос на конвертацию валют
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequestDto {

    /**
     * Код исходной валюты
     */
    @NotBlank(message = "Код валюты отправителя обязателен")
    private String fromCurrency;

    /**
     * Код целевой валюты
     */
    @NotBlank(message = "Код валюты получателя обязателен")
    private String toCurrency;

    /**
     * Сумма операции
     */
    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}