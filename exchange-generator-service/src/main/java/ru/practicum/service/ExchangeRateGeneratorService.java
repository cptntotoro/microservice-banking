package ru.practicum.service;

import reactor.core.publisher.Flux;
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
}
