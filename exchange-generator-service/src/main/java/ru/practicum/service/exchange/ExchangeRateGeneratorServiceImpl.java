package ru.practicum.service.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.practicum.client.ExchangeRateDto;
import ru.practicum.client.ExchangeServiceClient;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.model.currency.Currency;
import ru.practicum.model.exchange.ExchangeRate;
import ru.practicum.service.currency.CurrencyService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateGeneratorServiceImpl implements ExchangeRateGeneratorService {
    /**
     * Клиент для сервиса обмена валют
     */
    private final ExchangeServiceClient exchangeServiceClient;

    /**
     * Маппер курса обмена валюты
     */
    private final ExchangeRateMapper exchangeRateMapper;

    /**
     * Сервис для работы с валютами
     */
    private final CurrencyService currencyService;

    /**
     * Хранилище сгенерированных курсов
     */
    private final Map<String, ExchangeRate> currentRates = new ConcurrentHashMap<>();

    /**
     * Базовые курсы валют относительно RUB
     */
    private static final Map<String, BigDecimal> BASE_RATES_TO_RUB = Map.of(
            "USD", new BigDecimal("75.50"),
            "EUR", new BigDecimal("82.30"),
            "CNY", new BigDecimal("10.60"),
            "GBP", new BigDecimal("95.20"),
            "JPY", new BigDecimal("0.65"),
            "RUB", BigDecimal.ONE
    );

    @Override
    public Flux<ExchangeRate> getCurrentRates() {
        return Flux.fromIterable(currentRates.values())
                .doOnSubscribe(s -> log.debug("Предоставление текущих курсов Exchange Service"));
    }

    @Override
    public Flux<Currency> getAvailableCurrencies() {
        return currencyService.getAllCurrencies()
                .doOnSubscribe(s -> log.debug("Предоставление доступных валют через CurrencyService"));
    }

    @Scheduled(fixedRateString = "${exchange.rate.generation.interval.ms:1000}")
    public void generateRates() {
        log.debug("Запуск генерации новых курсов обмена (реактивно)");

        generateRatesReactively()
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .subscribe(
                        count -> log.info("Сгенерировано и отправлено курсов обмена: {}", count),
                        error -> log.error("Ошибка при генерации курсов обмена", error)
                );
    }

    private Mono<Integer> generateRatesReactively() {
        return currencyService.getAllCurrencies()
                .collectList()
                .flatMap(this::generateAndSendRates)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .doOnError(e -> log.error("Повторная попытка генерации провалилась", e))
                .onErrorResume(e -> {
                    log.error("Генерация курсов завершилась ошибкой, возвращаем 0", e);
                    return Mono.just(0);
                });
    }

    private Mono<Integer> generateAndSendRates(List<Currency> currencies) {
        // Очистка кэша атомарно
        currentRates.clear();

        List<String> currencyCodes = currencies.stream().map(Currency::getCode).collect(Collectors.toList());

        // Генерация курсов только относительно RUB
        return Flux.fromIterable(currencyCodes)
                .flatMap(currency -> generateRateToRUB(currency)
                        .doOnNext(rate -> currentRates.put(getRateKey(currency, "RUB"), rate))
                        .onErrorResume(e -> {
                            log.warn("Пропуск валюты {}: {}", currency, e.getMessage());
                            return Mono.empty();
                        }))
                .collectList()
                .flatMap(rates -> {
                    log.info("Сгенерировано {} курсов обмена относительно RUB", rates.size());
                    // Подготовка DTO для отправки
                    List<ExchangeRateDto> ratesDto = rates.stream()
                            .map(exchangeRateMapper::exchangeRateToExchangeRateDto)
                            .collect(Collectors.toList());
                    // Отправка и возврат количества
                    return exchangeServiceClient.sendExchangeRates(ratesDto)
                            .thenReturn(rates.size());
                });
    }

    private Mono<ExchangeRate> generateRateToRUB(String currency) {
        if (currency.equals("RUB")) {
            return Mono.just(ExchangeRate.builder()
                    .baseCurrency("RUB")
                    .targetCurrency("RUB")
                    .buyRate(BigDecimal.ONE)
                    .sellRate(BigDecimal.ONE)
                    .build());
        }

        if (!currencyService.isValidCurrency(currency)) {
            return Mono.error(new IllegalArgumentException("Неподдерживаемая валюта: " + currency));
        }

        BigDecimal baseRate = BASE_RATES_TO_RUB.getOrDefault(currency, BigDecimal.ONE);

        // Добавляем случайное отклонение ±1%
        BigDecimal randomFactor = BigDecimal.ONE.add(
                BigDecimal.valueOf((Math.random() * 0.02) - 0.01)
        );

        BigDecimal calculatedRate = baseRate.multiply(randomFactor)
                .setScale(4, RoundingMode.HALF_UP);

        // Спред 0.5%
        BigDecimal spread = calculatedRate.multiply(new BigDecimal("0.005"));

        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency(currency)
                .targetCurrency("RUB")
                .buyRate(calculatedRate.subtract(spread).setScale(4, RoundingMode.HALF_UP))
                .sellRate(calculatedRate.add(spread).setScale(4, RoundingMode.HALF_UP))
                .build();

        return Mono.just(rate);
    }

    private String getRateKey(String baseCurrency, String targetCurrency) {
        return baseCurrency + "_" + targetCurrency;
    }
}