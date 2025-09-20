package ru.practicum.mapper.currency;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.dto.CurrencyDto;
import ru.practicum.model.currency.Currency;

/**
 * Маппер валюты
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CurrencyMapper {

    /**
     * Смаппить модель валюты в DTO валюты
     *
     * @param currency Модель валюты
     * @return DTO валюты
     */
    CurrencyDto currencyToCurrencyDto(Currency currency);
}