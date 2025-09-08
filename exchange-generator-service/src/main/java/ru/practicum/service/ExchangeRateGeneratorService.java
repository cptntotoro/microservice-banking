package ru.practicum.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.ExchangeRate;

/**
 * Сервис генерации курсов обмена валют
 */
public interface ExchangeRateGeneratorService {

    // REST endpoint для получения текущих курсов

    /**
     * Получить текущие курсы обмена валют
     *
     * @return Список курсов обмена валют
     */
    Flux<ExchangeRate> getCurrentRates();

    // REST endpoint для получения конкретного курса

    /**
     * Получить курс обмена валюты
     *
     * @param baseCurrency Исходная валюта
     * @param targetCurrency Целевая валюта
     * @return Курс обмена валют
     */
    Mono<ExchangeRate> getRate(String baseCurrency, String targetCurrency);

    /**
     * Получить доступные валюты
     *
     * @return
     */
    Flux<String> getAvailableCurrencies();
}
