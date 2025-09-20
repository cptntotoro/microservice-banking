package ru.practicum.service.currency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.practicum.config.CurrencyConfig;
import ru.practicum.model.currency.Currency;

import java.util.Map;

/**
 * Реализация сервиса для работы с валютами
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyConfig currencyConfig;

    // Маппинг кодов валют на их названия
    private static final Map<String, String> CURRENCY_NAMES = Map.of(
            "RUB", "Russian Ruble",
            "USD", "US Dollar",
            "EUR", "Euro",
            "CNY", "Chinese Yuan",
            "GBP", "British Pound",
            "JPY", "Japanese Yen"
    );

    @Override
    public Flux<Currency> getAllCurrencies() {
        log.debug("Предоставление всех доступных валют из конфигурации");

        return Flux.fromIterable(currencyConfig.getSupported())
                .map(code -> new Currency(code, CURRENCY_NAMES.getOrDefault(code, code)));
    }

    @Override
    public boolean isValidCurrency(String code) {
        return currencyConfig.getSupported().contains(code);
    }
}