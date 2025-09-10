package ru.practicum.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Курс обмена валюты
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    /**
     * Исходная валюта
     */
    private String baseCurrency;

    /**
     * Целевая валюта конвертации
     */
    private String targetCurrency;

    /**
     * Цена покупки
     */
    private BigDecimal buyRate;

    /**
     * Цена продажи
     */
    private BigDecimal sellRate;
}