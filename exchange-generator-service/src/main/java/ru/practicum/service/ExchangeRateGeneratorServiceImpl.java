package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.Currency;
import ru.practicum.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateGeneratorServiceImpl implements ExchangeRateGeneratorService {

    private static final List<String> CURRENCIES = Arrays.asList(Currency.getAllCodes());
    private static final Currency BASE_CURRENCY = Currency.RUB;

    // In-memory хранилище сгенерированных курсов
    private final Map<String, ExchangeRate> currentRates = new ConcurrentHashMap<>();

    @Override
    public Flux<ExchangeRate> getCurrentRates() {
        return Flux.fromIterable(currentRates.values())
                .doOnSubscribe(s -> log.debug("Providing current rates to Exchange Service"));
    }

    @Override
    public Mono<ExchangeRate> getRate(String baseCurrency, String targetCurrency) {
        String key = getRateKey(baseCurrency, targetCurrency);
        ExchangeRate rate = currentRates.get(key);

        if (rate != null) {
            return Mono.just(rate);
        }

        // Если курса нет, генерируем его
        return Mono.fromCallable(() -> {
            ExchangeRate newRate = generateRate(baseCurrency, targetCurrency);
            currentRates.put(key, newRate);
            return newRate;
        });
    }

    @Override
    public Flux<String> getAvailableCurrencies() {
        return Flux.fromIterable(CURRENCIES);
    }

    @Scheduled(fixedRate = 1000)
    private void generateRates() {
        log.debug("Generating new exchange rates");

        List<ExchangeRate> newRates = new ArrayList<>();

        // Генерируем курсы относительно RUB
        for (String currency : CURRENCIES) {
            // RUB -> Валюта
            ExchangeRate rubToCurrency = generateRate(String.valueOf(BASE_CURRENCY), currency);
            newRates.add(rubToCurrency);
            currentRates.put(getRateKey(String.valueOf(BASE_CURRENCY), currency), rubToCurrency);

            // Валюта -> RUB (обратный курс)
            ExchangeRate currencyToRub = generateInverseRate(rubToCurrency);
            newRates.add(currencyToRub);
            currentRates.put(getRateKey(currency, String.valueOf(BASE_CURRENCY)), currencyToRub);
        }

        log.info("Generated {} exchange rates", newRates.size());
    }

    private ExchangeRate generateRate(String baseCurrency, String targetCurrency) {
        // Базовые значения для основных валют
        Map<String, BigDecimal> baseValues = Map.of(
                "USD", new BigDecimal("75.50"),
                "EUR", new BigDecimal("82.30"),
                "CNY", new BigDecimal("10.60"),
                "GBP", new BigDecimal("95.20"),
                "JPY", new BigDecimal("0.65")
        );

        BigDecimal baseRate = baseValues.getOrDefault(targetCurrency, new BigDecimal("1.00"));

        // Случайное отклонение ±1%
        BigDecimal randomFactor = BigDecimal.ONE.add(
                BigDecimal.valueOf((Math.random() * 0.02) - 0.01)
        );

        BigDecimal calculatedRate = baseRate.multiply(randomFactor)
                .setScale(4, RoundingMode.HALF_UP);

        // Спред 0.5% между покупкой и продажей
        BigDecimal spread = calculatedRate.multiply(new BigDecimal("0.005"));

        return ExchangeRate.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .buyRate(calculatedRate.subtract(spread).setScale(4, RoundingMode.HALF_UP))
                .sellRate(calculatedRate.add(spread).setScale(4, RoundingMode.HALF_UP))
                .build();
    }

    private ExchangeRate generateInverseRate(ExchangeRate originalRate) {
        BigDecimal inverseBuy = BigDecimal.ONE.divide(originalRate.getSellRate(), 4, RoundingMode.HALF_UP);
        BigDecimal inverseSell = BigDecimal.ONE.divide(originalRate.getBuyRate(), 4, RoundingMode.HALF_UP);

        return ExchangeRate.builder()
                .baseCurrency(originalRate.getTargetCurrency())
                .targetCurrency(originalRate.getBaseCurrency())
                .buyRate(inverseBuy)
                .sellRate(inverseSell)
                .build();
    }

    private String getRateKey(String baseCurrency, String targetCurrency) {
        return baseCurrency + "_" + targetCurrency;
    }
}