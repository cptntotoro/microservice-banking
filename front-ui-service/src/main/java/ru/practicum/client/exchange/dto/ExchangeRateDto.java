package ru.practicum.client.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Клиентский DTO курса обмена валют
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDto {
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
