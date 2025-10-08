package ru.practicum.service.exchange;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.client.exchange.ExchangeServiceClient;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.model.exchange.ExchangeRate;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Сервис для работы с курсами валют
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {
    /**
     * Клиент для сервиса обмена валют
     */
    private final ExchangeServiceClient exchangeServiceClient;

    /**
     * Маппер операций с наличными
     */
    private final ExchangeRateMapper exchangeRateMapper;

    /**
     * Список доступных валют
     */
    @Value("${app.currencies.supported:RUB,USD,EUR,CNY,GBP,JPY}")
    private final Set<String> availableCurrencies = new CopyOnWriteArraySet<>();

    @PostConstruct
    public void init() {
        log.info("Инициализация доступных валют при запуске сервиса");

        // Асинхронная загрузка валют
        loadAvailableCurrencies()
                .subscribe(
                        currencies -> {
                            availableCurrencies.addAll(currencies);
                            log.info("Инициализировано {} доступных валют: {}", availableCurrencies.size(), availableCurrencies);
                        },
                        error -> {
                            log.warn("Не удалось загрузить курсы валют при инициализации: {}", error.getMessage());
                            // Добавляем дефолтные валюты в случае ошибки
                            availableCurrencies.addAll(Set.of("RUB", "USD", "EUR", "CNY"));
                            log.info("Использованы дефолтные валюты: {}", availableCurrencies);
                        }
                );
    }

    /**
     * Асинхронная загрузка доступных валют
     */
    private Mono<Set<String>> loadAvailableCurrencies() {
        return getCurrentRates()
                .collectList()
                .map(rates -> {
                    Set<String> currencies = new CopyOnWriteArraySet<>();
                    rates.forEach(rate -> {
                        try {
                            currencies.add(rate.getBaseCurrency());
                            currencies.add(rate.getTargetCurrency());
                        } catch (IllegalArgumentException e) {
                            log.warn("Неизвестная валюта в курсах: {} или {}", rate.getBaseCurrency(), rate.getTargetCurrency());
                        }
                    });
                    return currencies;
                })
                .onErrorResume(e -> {
                    log.error("Ошибка при загрузке доступных валют: {}", e.getMessage());
                    return Mono.just(Collections.emptySet());
                });
    }

    @Override
    public Flux<ExchangeRate> getCurrentRates() {
        log.info("Получение текущих курсов валют");

        return exchangeServiceClient.getCurrentRates()
                .map(exchangeRateMapper::exchangeRateClientDtoToExchangeRate)
                .doOnNext(rate -> log.debug("Получен курс: {} -> {}", rate.getBaseCurrency(), rate.getTargetCurrency()))
                .doOnComplete(() -> log.info("Все курсы успешно загружены"))
                .doOnError(error -> log.error("Ошибка при загрузке курсов: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Не удалось загрузить курсы валют, возвращаем пустой поток");
                    return Flux.empty();
                });
    }

    @Override
    public Flux<String> getAvailableCurrencies() {
        return Flux.fromIterable(availableCurrencies)
                .doOnComplete(() -> log.debug("Возвращены доступные валюты: {}", availableCurrencies));
    }
}