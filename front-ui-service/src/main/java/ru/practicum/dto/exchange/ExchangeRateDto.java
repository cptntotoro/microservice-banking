package ru.practicum.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO курса обмена валют
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDto {
    /**
     * Код валюты относительно базовой
     */
    private String code;

    /**
     * Значение курса покупки
     */
    private BigDecimal buyValue;

    /**
     * Значение курса продажи
     */
    private BigDecimal sellValue;
}