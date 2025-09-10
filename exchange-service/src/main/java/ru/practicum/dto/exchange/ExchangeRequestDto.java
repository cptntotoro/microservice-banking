package ru.practicum.dto.exchange;

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
     * Исходная валюта
     */
    @NotNull
    private String fromCurrency;

    /**
     * Целевая валюта
     */
    @NotNull
    private String toCurrency;

    /**
     * Сумма для конвертации
     */
    @NotNull
    @Positive
    private BigDecimal amount;

    /**
     * Покупка / Продажа
     */
    @NotNull
    private OperationType operationType;

    /**
     * Идентификатор пользователя
     */
    @NotNull
    private UUID userId;
}
