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
     * Исходная валюта
     */
    private String fromCurrency;

    /**
     * Целевая валюта
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

    /**
     * Использованный курс обмена
     */
    private BigDecimal exchangeRate;

    /**
     * Тип операции (покупка/продажа)
     */
    private String operationType;
}