package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.practicum.client.ExchangeServiceClient;
import ru.practicum.client.ExchangeRateDto;
import ru.practicum.mapper.ExchangeRateMapper;
import ru.practicum.model.Currency;
import ru.practicum.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateGeneratorServiceImpl implements ExchangeRateGeneratorService {
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

    /**
     * Клиент для сервиса обмена валют
     */
    private final ExchangeServiceClient exchangeServiceClient;

    /**
     * Маппер курса обмена валюты
     */
    private final ExchangeRateMapper exchangeRateMapper;

    @Override
    public Flux<ExchangeRate> getCurrentRates() {
        return Flux.fromIterable(currentRates.values())
                .doOnSubscribe(s -> log.debug("Providing current rates to Exchange Service"));
    }

    @Scheduled(fixedRateString = "${exchange.rate.generation.interval.ms:1000}")
    private void generateRates() {
        log.debug("Generating new exchange rates");

        currentRates.clear();

        // Получаем все валюты из Enum
        String[] currencies = Currency.getAllCodes();

        // Генерируем все возможные пары (включая кросс-курсы и одинаковые)
        for (String base : currencies) {
            for (String target : currencies) {
                try {
                    ExchangeRate rate = generateRate(base, target);
                    currentRates.put(getRateKey(base, target), rate);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping pair {} to {}: {}", base, target, e.getMessage());
                }
            }
        }

        log.info("Generated {} exchange rates", currentRates.size());

        // Отправка в Exchange через клиент
        List<ExchangeRateDto> ratesDto = currentRates.values().stream()
                .map(exchangeRateMapper::exchangeRateToExchangeRateDto)
                .collect(Collectors.toList());
        exchangeServiceClient.sendExchangeRates(ratesDto)
                .subscribe();
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
        if (!Currency.isValidCurrency(baseCurrency) || !Currency.isValidCurrency(targetCurrency)) {
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

    private String getRateKey(String baseCurrency, String targetCurrency) {
        return baseCurrency + "_" + targetCurrency;
    }
}