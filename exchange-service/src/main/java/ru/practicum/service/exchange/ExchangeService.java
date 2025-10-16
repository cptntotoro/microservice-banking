package ru.practicum.service.exchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.exchange.ExchangeRate;

import java.math.BigDecimal;

/**
 * Сервис обмена валют
 */
public interface ExchangeService {

    /**
     * Получить текущие курсы валют
     *
     * @return Список курсов обмена валют
     */
    Flux<ExchangeRate> getCurrentRates();

    /**
     * Получить курс обмена валют
     *
     * @param fromCurrency Исходная валюта
     * @param toCurrency   Целевая валюта
     * @return Значение курса обмена валют
     */
    Mono<ExchangeRate> getRate(String fromCurrency, String toCurrency);

    /**
     * Конвертировать валюту
     *
     * @param fromCurrency Исходная валюта
     * @param toCurrency   Целевая валюта
     * @param amount       Количество средств
     * @return Значение конвертации
     */
    Mono<BigDecimal> convert(String fromCurrency, String toCurrency, BigDecimal amount);

    /**
     * Получить доступные валюты для конвертации
     *
     * @return Список доступных валюты
     */
    Flux<String> getAvailableCurrencies();

    /**
     * Получить курсы валют из exchange-generator-service
     *
     * @param rates Курсы обмена валюты
     */
    Mono<Void> updateRatesFromGenerator(Flux<ExchangeRate> rates);
}
