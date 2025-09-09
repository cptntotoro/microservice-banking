package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.CurrencyDto;
import ru.practicum.model.Currency;

/**
 * Маппер валюты
 */
@Mapper(componentModel = "spring")
public interface CurrencyMapper {

    /**
     * Смаппить enum валюты в DTO валюты
     */
    CurrencyDto currencyToCurrencyDto(Currency currency);
}