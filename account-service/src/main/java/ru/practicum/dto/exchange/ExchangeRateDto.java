package ru.practicum.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO курса валют
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateDto {

    private UUID id;
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private LocalDate effectiveDate;
}