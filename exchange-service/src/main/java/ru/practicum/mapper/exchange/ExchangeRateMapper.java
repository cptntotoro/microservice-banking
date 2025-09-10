package ru.practicum.mapper.exchange;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.model.exchange.ExchangeRate;
import ru.practicum.dto.exchange.ExchangeResponseDto;

import java.math.BigDecimal;

/**
 * Маппер курса обмена валют
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExchangeRateMapper {

    /**
     * Смаппить DTO курса обмена валют в курс обмена валют
     *
     * @param exchangeRateDto DTO курса обмена валют
     * @return Курс обмена валют
     */
    ExchangeRate exchangeRateDtoToExchangeRate(ExchangeRateDto exchangeRateDto);

    /**
     * Смаппить курс обмена валют в DTO курса обмена валют
     *
     * @param exchangeRate Курс обмена валют
     * @return DTO курса обмена валют
     */
    ExchangeRateDto exchangeRateToExchangeRateDto(ExchangeRate exchangeRate);

    /**
     * Создать DTO ответа из результата конвертации
     */
    @Mapping(target = "operationType", constant = "BUY")
    ExchangeResponseDto toResponseDto(String fromCurrency, String toCurrency,
                                      BigDecimal originalAmount, BigDecimal convertedAmount, BigDecimal exchangeRate);
}