package ru.practicum.mapper.exchange;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.practicum.client.exchange.ExchangeRateClientDto;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.model.exchange.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер операций с наличными
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ExchangeRateMapper {

    /**
     * Смаппить DTO ответа сервиса генерации курса обмена валют в модель
     *
     * @param exchangeRateClientDto DTO ответа сервиса генерации курса обмена валют
     * @return Курс обмена валют
     */
    ExchangeRate exchangeRateClientDtoToExchangeRate(ExchangeRateClientDto exchangeRateClientDto);

    /**
     * Смаппить курс обмена валют в DTO для отображения
     *
     * @param exchangeRate Курс обмена валют
     * @return DTO курса обмена валют
     */
    default ExchangeRateDto exchangeRateToExchangeRateDto(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            return null;
        }

        String currencyPair = exchangeRate.getBaseCurrency() + "/" + exchangeRate.getTargetCurrency();

        return ExchangeRateDto.builder()
                .code(currencyPair)
                .buyValue(exchangeRate.getBuyRate() != null ? exchangeRate.getBuyRate() : BigDecimal.ZERO)
                .sellValue(exchangeRate.getSellRate() != null ? exchangeRate.getSellRate() : BigDecimal.ZERO)
                .build();
    }

    /**
     * Смаппить список курсов обмена валют в список DTO курсов обмена валют
     *
     * @param exchangeRates Список курсов обмена валют
     * @return Список DTO курсов обмена валют
     */
    default List<ExchangeRateDto> toExchangeRateDtoList(List<ExchangeRate> exchangeRates) {
        if (exchangeRates == null) {
            return List.of();
        }

        return exchangeRates.stream()
                .map(this::exchangeRateToExchangeRateDto)
                .collect(Collectors.toList());
    }
}