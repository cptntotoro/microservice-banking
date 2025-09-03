package ru.practicum.mapper.currency;

import org.mapstruct.Mapper;
import ru.practicum.dao.currency.CurrencyDao;
import ru.practicum.model.currency.Currency;

/**
 * Маппер валют
 */
@Mapper(componentModel = "spring")
public interface CurrencyMapper {

    /**
     * Смаппить валюту в DAO валюты
     *
     * @param currency Валюта
     * @return DAO валюты
     */
    CurrencyDao currencyToCurrencyDao(Currency currency);

    /**
     * Смаппить DAO валюты в валюту
     *
     * @param currencyDao DAO валюты
     * @return Валюта
     */
    Currency currencyDaoToCurrency(CurrencyDao currencyDao);
}
