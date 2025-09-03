package ru.practicum.model.exchange;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Курс валют
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Идентификатор базовой валюты
     */
    private UUID baseCurrencyId;

    /**
     * Идентификатор целевой валюты
     */
    private UUID targetCurrencyId;

    /**
     * Курс покупки
     */
    private BigDecimal buyRate;

    /**
     * Курс продажи
     */
    private BigDecimal sellRate;

    /**
     * Дата действия курса
     */
    private LocalDate effectiveDate;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}
