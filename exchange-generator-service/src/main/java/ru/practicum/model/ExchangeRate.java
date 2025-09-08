package ru.practicum.model;

import lombok.*;

import java.math.BigDecimal;

/**
 * Модель курса обмена валюты
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