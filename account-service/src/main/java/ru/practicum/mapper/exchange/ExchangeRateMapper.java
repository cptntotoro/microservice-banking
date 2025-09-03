package ru.practicum.mapper.exchange;

import org.mapstruct.Mapper;
import ru.practicum.dao.exchange.ExchangeRateDao;
import ru.practicum.model.exchange.ExchangeRate;

/**
 * Маппер курсов валют
 */
@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {

    /**
     * Смаппить курс валют в DAO курса валют
     *
     * @param exchangeRate Курс валют
     * @return DAO курса валют
     */
    ExchangeRateDao exchangeRateToExchangeRateDao(ExchangeRate exchangeRate);

    /**
     * Смаппить DAO курса валют в курс валют
     *
     * @param exchangeRateDao DAO курса валют
     * @return Курс валют
     */
    ExchangeRate exchangeRateDaoToExchangeRate(ExchangeRateDao exchangeRateDao);
}
