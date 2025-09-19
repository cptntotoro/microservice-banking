package ru.practicum.client.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для курса валют
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDto {

    /**
     * Код валюты отправителя
     */
    private String fromCurrency;

    /**
     * Код валюты получателя
     */
    private String toCurrency;

    /**
     * Курс покупки
     */
    private BigDecimal buyRate;

    /**
     * Курс продажи
     */
    private BigDecimal sellRate;

    /**
     * Время обновления курса
     */
    private LocalDateTime updatedAt;
}