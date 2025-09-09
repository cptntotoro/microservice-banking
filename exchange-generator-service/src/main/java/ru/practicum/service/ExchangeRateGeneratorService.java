package ru.practicum.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.ExchangeRate;

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
     * @return Коды доступных валют
     */
    Flux<String> getAvailableCurrencies();
}
