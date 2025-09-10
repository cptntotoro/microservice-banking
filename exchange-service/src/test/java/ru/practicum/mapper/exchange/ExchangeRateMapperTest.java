package ru.practicum.mapper.exchange;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.model.exchange.ExchangeRate;
import ru.practicum.dto.exchange.ExchangeResponseDto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExchangeRateMapperTest {

    private final ExchangeRateMapper mapper = Mappers.getMapper(ExchangeRateMapper.class);

    @Test
    void exchangeRateDtoToExchangeRate() {
        ExchangeRateDto dto = new ExchangeRateDto();
        dto.setBaseCurrency("USD");
        dto.setTargetCurrency("RUB");
        dto.setBuyRate(new BigDecimal("75.50"));
        dto.setSellRate(new BigDecimal("76.00"));

        ExchangeRate result = mapper.exchangeRateDtoToExchangeRate(dto);

        assertNotNull(result);
        assertEquals("USD", result.getBaseCurrency());
        assertEquals("RUB", result.getTargetCurrency());
        assertEquals(new BigDecimal("75.50"), result.getBuyRate());
        assertEquals(new BigDecimal("76.00"), result.getSellRate());
    }

    @Test
    void exchangeRateToExchangeRateDto() {
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("85.30"))
                .sellRate(new BigDecimal("85.80"))
                .build();

        ExchangeRateDto result = mapper.exchangeRateToExchangeRateDto(rate);

        assertNotNull(result);
        assertEquals("EUR", result.getBaseCurrency());
        assertEquals("RUB", result.getTargetCurrency());
        assertEquals(new BigDecimal("85.30"), result.getBuyRate());
        assertEquals(new BigDecimal("85.80"), result.getSellRate());
    }

    @Test
    void toResponseDto() {
        String fromCurrency = "USD";
        String toCurrency = "RUB";
        BigDecimal originalAmount = new BigDecimal("100.00");
        BigDecimal convertedAmount = new BigDecimal("7550.00");
        BigDecimal exchangeRate = new BigDecimal("75.50");

        ExchangeResponseDto result = mapper.toResponseDto(fromCurrency, toCurrency, originalAmount, convertedAmount, exchangeRate);

        assertNotNull(result);
        assertEquals("USD", result.getFromCurrency());
        assertEquals("RUB", result.getToCurrency());
        assertEquals(new BigDecimal("100.00"), result.getOriginalAmount());
        assertEquals(new BigDecimal("7550.00"), result.getConvertedAmount());
        assertEquals(new BigDecimal("75.50"), result.getExchangeRate());
        assertEquals("BUY", result.getOperationType());
    }
}