package ru.practicum.client.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для ответа конвертации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertResponseDto {

    /**
     * Исходная сумма
     */
    private BigDecimal originalAmount;

    /**
     * Исходная валюта
     */
    private String fromCurrency;

    /**
     * Конвертированная сумма
     */
    private BigDecimal convertedAmount;

    /**
     * Целевая валюта
     */
    private String toCurrency;

    /**
     * Использованный курс
     */
    private BigDecimal exchangeRate;

    /**
     * Время конвертации
     */
    private java.time.LocalDateTime convertedAt;
}