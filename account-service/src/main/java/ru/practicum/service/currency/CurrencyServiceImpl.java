package ru.practicum.service.currency;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.config.CurrencyConfig;
import ru.practicum.dao.currency.CurrencyDao;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.currency.CurrencyMapper;
import ru.practicum.model.currency.Currency;
import ru.practicum.repository.currency.CurrencyRepository;

import java.util.Map;
import java.util.UUID;

/**
 * Реализация сервиса для работы с валютами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
    /**
     * Репозиторий валют
     */
    private final CurrencyRepository currencyRepository;

    /**
     * Маппер валют
     */
    private final CurrencyMapper currencyMapper;

    /**
     * Конфигурация валют
     */
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

    @PostConstruct
    public void initCurrencies() {
        log.info("Начало инициализации валют из конфигурации");

        Flux.fromIterable(currencyConfig.getSupported())
                .flatMap(this::createCurrencyIfNotExists)
                .subscribe(
                        currency -> log.info("Валюта инициализирована: {} - {}", currency.getCode(), currency.getName()),
                        error -> log.error("Ошибка при инициализации валют: ", error),
                        () -> log.info("Инициализация валют завершена")
                );
    }

    private Mono<Currency> createCurrencyIfNotExists(String code) {
        return getCurrencyByCode(code)
                .switchIfEmpty(Mono.defer(() -> {
                    String name = CURRENCY_NAMES.getOrDefault(code, code);
                    Currency currency = Currency.builder()
                            .id(UUID.randomUUID())
                            .code(code)
                            .name(name)
                            .build();
                    return createCurrency(currency);
                }))
                .onErrorResume(e -> {
                    log.warn("Ошибка при создании валюты {}: {}", code, e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Currency> createCurrency(Currency currency) {
        log.debug("Создание валюты: {}", currency.getCode());

        CurrencyDao dao = currencyMapper.currencyToCurrencyDao(currency);
        return currencyRepository.save(dao)
                .map(currencyMapper::currencyDaoToCurrency)
                .doOnSuccess(c -> log.info("Валюта создана: {} - {}", c.getCode(), c.getName()))
                .doOnError(e -> log.error("Ошибка при создании валюты {}: {}", currency.getCode(), e.getMessage()));
    }

    @Override
    public Mono<Currency> getCurrencyById(UUID id) {
        log.debug("Получение валюты по ID: {}", id);

        return currencyRepository.findById(id)
                .map(currencyMapper::currencyDaoToCurrency)
                .switchIfEmpty(Mono.error(new NotFoundException("Валюта", id.toString())));
    }

    @Override
    public Mono<Currency> getCurrencyByCode(String code) {
        log.debug("Получение валюты по коду: {}", code);

        return currencyRepository.findByCode(code)
                .map(currencyMapper::currencyDaoToCurrency)
                .switchIfEmpty(Mono.error(new NotFoundException("Валюта", code)));
    }

    @Override
    public Flux<Currency> getAllCurrencies() {
        log.debug("Получение всех валют");

        return currencyRepository.findAll()
                .map(currencyMapper::currencyDaoToCurrency);
    }

    @Override
    public Mono<Boolean> isValidCurrency(String code) {
        return currencyRepository.findByCode(code)
                .map(currency -> true)
                .defaultIfEmpty(false);
    }

    /**
     * Получить название валюты по коду
     */
    public String getCurrencyName(String code) {
        return CURRENCY_NAMES.getOrDefault(code, code);
    }
}