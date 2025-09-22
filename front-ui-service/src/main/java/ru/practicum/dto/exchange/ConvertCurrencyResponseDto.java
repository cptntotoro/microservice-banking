package ru.practicum.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvertCurrencyResponseDto {
    /**
     * Код исходной валюты
     */
    private String fromCurrency;

    /**
     * Код целевой валюты
     */
    private String toCurrency;
    private BigDecimal originalAmount;

    /**
     * Сумма целевой операции
     */
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;
    private BigDecimal commission;
    private LocalDateTime conversionDate;
}
