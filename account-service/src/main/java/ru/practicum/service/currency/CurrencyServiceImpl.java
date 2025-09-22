package ru.practicum.service.currency;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.client.ExchangeServiceClient;
import ru.practicum.dao.currency.CurrencyDao;
import ru.practicum.exception.ErrorReasons;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.currency.CurrencyMapper;
import ru.practicum.model.currency.Currency;
import ru.practicum.repository.currency.CurrencyRepository;

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
     * Клиент для обращения к сервису обмена валют
     */
    private final ExchangeServiceClient exchangeServiceClient;

    @PostConstruct
    public void initCurrencies() {
        exchangeServiceClient.getAvailableCurrencies()
                .flatMapMany(dto -> Flux.fromIterable(dto.getCurrencies()))
                .flatMap(this::createCurrencyIfNotExists)
                .subscribe(
                        currency -> log.info("Валюта инициализирована: {}", currency.getCode()),
                        error -> log.error("Ошибка при инициализации валют: ", error)
                );
    }

    private Mono<Currency> createCurrencyIfNotExists(String code) {
        return getCurrencyByCode(code)
                .switchIfEmpty(Mono.defer(() -> {
                    Currency currency = Currency.builder()
                            .id(UUID.randomUUID())
                            .code(code)
                            .name(code)
                            .build();
                    return createCurrency(currency);
                }));
    }

    @Override
    public Mono<Currency> createCurrency(Currency currency) {
        return checkIfInitialized()
                .then(Mono.defer(() -> {
                    CurrencyDao dao = currencyMapper.currencyToCurrencyDao(currency);
                    return currencyRepository.save(dao)
                            .map(currencyMapper::currencyDaoToCurrency);
                }));
    }

    @Override
    public Mono<Currency> getCurrencyById(UUID id) {
        return checkIfInitialized()
                .then(currencyRepository.findById(id)
                        .map(currencyMapper::currencyDaoToCurrency));
    }

    @Override
    public Mono<Currency> getCurrencyByCode(String code) {
        return checkIfInitialized()
                .then(currencyRepository.findByCode(code)
                        .map(currencyMapper::currencyDaoToCurrency));
    }

    @Override
    public Flux<Currency> getAllCurrencies() {
        return checkIfInitialized()
                .thenMany(currencyRepository.findAll()
                        .map(currencyMapper::currencyDaoToCurrency));
    }

    private Mono<Void> checkIfInitialized() {
        return currencyRepository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        return Mono.error(new ValidationException(
                                "Валюты не инициализированы. Пожалуйста, подождите или проверьте подключение к exchange-service.",
                                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                                ErrorReasons.CURRENCIES_NOT_INITIALIZED
                        ));
                    }
                    return Mono.empty();
                });
    }
}