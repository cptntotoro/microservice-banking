package ru.practicum.mapper.exchange;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.client.exchange.dto.ExchangeRateDto;
import ru.practicum.model.exchange.ExchangeRate;

/**
 * Маппер курса обмена валюты
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExchangeRateMapper {

    /**
     * Смаппить модель курса обмена валюты в DTO курса обмена валюты
     *
     * @param exchangeRate Модель курса обмена валюты
     * @return DTO курса обмена валюты
     */
    ExchangeRateDto exchangeRateToExchangeRateDto(ExchangeRate exchangeRate);
}