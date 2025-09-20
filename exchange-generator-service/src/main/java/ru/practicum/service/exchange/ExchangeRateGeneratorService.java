package ru.practicum.service.exchange;

import reactor.core.publisher.Flux;
import ru.practicum.model.currency.Currency;
import ru.practicum.model.exchange.ExchangeRate;

/**
 * Сервис генерации курсов обмена валют
 */
public interface ExchangeRateGeneratorService {

    /**
     * Получить текущие курсы обмена валют
     *
     * @return Список курсов обмена валют
     */
    Flux<ExchangeRate> getCurrentRates();

    /**
     * Получить доступные валюты
     *
     * @return Список валют
     */
    Flux<Currency> getAvailableCurrencies();
}
