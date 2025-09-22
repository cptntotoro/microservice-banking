package ru.practicum.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.operation.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO запроса на конвертацию валют
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequestDto {
    /**
     * Код исходной валюты
     */
    private String fromCurrency;

    /**
     * Код целевой валюты
     */
    private String toCurrency;

    /**
     * Сумма операции
     */
    private BigDecimal amount;
    private OperationType operationType;
    private UUID userId;
}