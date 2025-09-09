package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateGeneratorServiceImpl implements ExchangeRateGeneratorService {
    /**
     * Список поддерживаемых валют
     */
    private static final List<String> SUPPORTED_CURRENCIES = Arrays.asList("RUB", "USD", "CNY", "EUR", "GBP", "JPY");

    /**
     * Базовая валюта, относительно которой происходит конвертация
     */
    private static final String BASE_CURRENCY_CODE = "RUB";

    /**
     * Хранилище сгенерированных курсов
     */
    private final Map<String, ExchangeRate> currentRates = new ConcurrentHashMap<>();

    private static final Map<String, BigDecimal> BASE_RATES = Map.of(
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
                .doOnSubscribe(s -> log.debug("Providing current rates to Exchange Service"));
    }

    @Override
    public Mono<ExchangeRate> getRate(String baseCurrency, String targetCurrency) {
        // Проверяем, что обе валюты поддерживаются
        if (!isCurrencySupported(baseCurrency)) {
            return Mono.error(new IllegalArgumentException("Base currency not supported: " + baseCurrency));
        }
        if (!isCurrencySupported(targetCurrency)) {
            return Mono.error(new IllegalArgumentException("Target currency not supported: " + targetCurrency));
        }

        String key = getRateKey(baseCurrency, targetCurrency);
        ExchangeRate rate = currentRates.get(key);

        if (rate != null) {
            return Mono.just(rate);
        }

        // Если курса нет в кэше, генерируем его по требованию
        return Mono.fromCallable(() -> {
            // Дополнительная проверка внутри callable
            if (!isCurrencySupported(baseCurrency) || !isCurrencySupported(targetCurrency)) {
                throw new IllegalArgumentException("Unsupported currency pair: " + baseCurrency + " to " + targetCurrency);
            }
            return generateRate(baseCurrency, targetCurrency);
        });
    }

    @Override
    public Flux<String> getAvailableCurrencies() {
        return Flux.fromIterable(SUPPORTED_CURRENCIES);
    }

    @Scheduled(fixedRateString = "${exchange.rate.generation.interval.ms:1000}")
    public void generateRates() {
        log.debug("Generating new exchange rates");

        currentRates.clear();

        // Генерируем только курсы для поддерживаемых валют
        for (String currency : SUPPORTED_CURRENCIES) {
            if (!currency.equals(BASE_CURRENCY_CODE) && isCurrencySupported(currency)) {
                try {
                    // RUB -> Валюта (прямой курс)
                    ExchangeRate rubToCurrency = generateRate(BASE_CURRENCY_CODE, currency);
                    currentRates.put(getRateKey(BASE_CURRENCY_CODE, currency), rubToCurrency);

                    // Валюта -> RUB (обратный курс)
                    ExchangeRate currencyToRub = generateInverseRate(rubToCurrency);
                    currentRates.put(getRateKey(currency, BASE_CURRENCY_CODE), currencyToRub);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping currency {}: {}", currency, e.getMessage());
                }
            }
        }

        // Генерируем курс RUB -> RUB
        ExchangeRate rubToRub = ExchangeRate.builder()
                .baseCurrency(BASE_CURRENCY_CODE)
                .targetCurrency(BASE_CURRENCY_CODE)
                .buyRate(BigDecimal.ONE)
                .sellRate(BigDecimal.ONE)
                .build();
        currentRates.put(getRateKey(BASE_CURRENCY_CODE, BASE_CURRENCY_CODE), rubToRub);

        log.info("Generated {} exchange rates", currentRates.size());
    }

    private ExchangeRate generateRate(String baseCurrency, String targetCurrency) {
        // Для одинаковых валют
        if (baseCurrency.equals(targetCurrency)) {
            return ExchangeRate.builder()
                    .baseCurrency(baseCurrency)
                    .targetCurrency(targetCurrency)
                    .buyRate(BigDecimal.ONE)
                    .sellRate(BigDecimal.ONE)
                    .build();
        }

        // Проверяем, что обе валюты поддерживаются
        if (!isCurrencySupported(baseCurrency) || !isCurrencySupported(targetCurrency)) {
            throw new IllegalArgumentException("Unsupported currency pair: " + baseCurrency + " to " + targetCurrency);
        }

        // Получаем базовые курсы относительно RUB
        BigDecimal baseRate = BASE_RATES.getOrDefault(baseCurrency, BigDecimal.ONE);
        BigDecimal targetRate = BASE_RATES.getOrDefault(targetCurrency, BigDecimal.ONE);

        // Рассчитываем кросс-курс: (targetRate / baseRate)
        BigDecimal crossRate = targetRate.divide(baseRate, 6, RoundingMode.HALF_UP);

        // Добавляем случайное отклонение ±1%
        BigDecimal randomFactor = BigDecimal.ONE.add(
                BigDecimal.valueOf((Math.random() * 0.02) - 0.01)
        );

        BigDecimal calculatedRate = crossRate.multiply(randomFactor)
                .setScale(4, RoundingMode.HALF_UP);

        // Добавляем спред 0.5% между покупкой и продажей
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

    /**
     * Проверяет, поддерживается ли валюта
     */
    private boolean isCurrencySupported(String currencyCode) {
        return SUPPORTED_CURRENCIES.contains(currencyCode.toUpperCase());
    }
}