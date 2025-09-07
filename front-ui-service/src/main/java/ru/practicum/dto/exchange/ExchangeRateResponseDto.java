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
public class ExchangeRateResponseDto {
    private String currency;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private LocalDateTime lastUpdated;
    private BigDecimal changePercent;
}
