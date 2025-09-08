package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.ExchangeRateDto;
import ru.practicum.model.ExchangeRate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateMapperTest {

    private final ExchangeRateMapper mapper = Mappers.getMapper(ExchangeRateMapper.class);

    @Test
    void exchangeRateToExchangeRateDto_shouldMapAllFieldsCorrectly() {
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .baseCurrency("RUB")
                .targetCurrency("USD")
                .buyRate(new BigDecimal("75.00"))
                .sellRate(new BigDecimal("75.50"))
                .build();

        ExchangeRateDto dto = mapper.exchangeRateToExchangeRateDto(exchangeRate);

        assertThat(dto.getBaseCurrency()).isEqualTo("RUB");
        assertThat(dto.getTargetCurrency()).isEqualTo("USD");
        assertThat(dto.getBuyRate()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(dto.getSellRate()).isEqualByComparingTo(new BigDecimal("75.50"));
    }

    @Test
    void exchangeRateToExchangeRateDto_withNullValues_shouldHandleNulls() {
        ExchangeRate exchangeRate = ExchangeRate.builder()
                .baseCurrency(null)
                .targetCurrency("USD")
                .buyRate(null)
                .sellRate(new BigDecimal("75.50"))
                .build();

        ExchangeRateDto dto = mapper.exchangeRateToExchangeRateDto(exchangeRate);

        assertThat(dto.getBaseCurrency()).isNull();
        assertThat(dto.getTargetCurrency()).isEqualTo("USD");
        assertThat(dto.getBuyRate()).isNull();
        assertThat(dto.getSellRate()).isEqualByComparingTo(new BigDecimal("75.50"));
    }
}