package ru.practicum.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Ответ на запрос конвертации валют
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponseDto {
    /**
     * Код исходной валюты
     */
    private String fromCurrency;

    /**
     * Код целевой валюты
     */
    private String toCurrency;

    /**
     * Исходная сумма
     */
    private BigDecimal originalAmount;

    /**
     * Результат конвертации
     */
    private BigDecimal convertedAmount;
}