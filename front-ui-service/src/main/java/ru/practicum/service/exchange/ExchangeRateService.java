package ru.practicum.service.exchange;

import reactor.core.publisher.Flux;
import ru.practicum.model.exchange.ExchangeRate;

/**
 * Сервис курсов обмена валют
 */
public interface ExchangeRateService {
    /**
     * Получить текущие курсы валют
     *
     * @return Курсы обмена валют
     */
    Flux<ExchangeRate> getCurrentRates();
}
