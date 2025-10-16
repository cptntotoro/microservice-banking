package ru.practicum.service.currency;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.currency.Currency;

import java.util.UUID;

/**
 * Сервис для работы с валютами
 */
public interface CurrencyService {

    /**
     * Создать валюту
     *
     * @param currency Валюта
     * @return Созданная валюта
     */
    Mono<Currency> createCurrency(Currency currency);

    /**
     * Получить валюту по идентификатору
     *
     * @param currencyId Идентификатор валюты
     * @return Валюта
     */
    Mono<Currency> getCurrencyById(UUID currencyId);

    /**
     * Получить валюту по коду
     *
     * @param code Код валюты
     * @return Валюта
     */
    Mono<Currency> getCurrencyByCode(String code);

    /**
     * Получить все валюты
     *
     * @return Список валют
     */
    Flux<Currency> getAllCurrencies();

    /**
     * Проверить валидность кода валюты
     *
     * @param code Код валюты
     * @return Да / Нет
     */
    Mono<Boolean> isValidCurrency(String code);
}