package ru.practicum.service.currency;

import reactor.core.publisher.Flux;
import ru.practicum.model.currency.Currency;

/**
 * Сервис для работы с валютами
 */
public interface CurrencyService {

    /**
     * Получить все доступные валюты
     *
     * @return Список валют
     */
    Flux<Currency> getAllCurrencies();

    boolean isValidCurrency(String code);
}