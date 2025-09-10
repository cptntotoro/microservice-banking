package ru.practicum.service.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.model.exchange.ExchangeRate;
import ru.practicum.model.operation.Operation;
import ru.practicum.model.operation.OperationType;
import ru.practicum.service.operation.OperationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeServiceImpl implements ExchangeService {

    /**
     * Маппер курса обмена валют
     */
    private final ExchangeRateMapper exchangeRateMapper;

    /**
     * Сервис управления валютными операциями
     */
    private final OperationService operationService;

    /**
     * In-memory кэш курсов
     */
    private final Map<String, ExchangeRate> ratesCache = new ConcurrentHashMap<>();

    /**
     * Доступные валюты
     */
    private List<String> availableCurrencies = List.of();

    @Value("${exchange.spread:0.005}")
    private BigDecimal spread;

    @Value("${exchange.scale:4}")
    private int scale;

    @Override
    public Flux<ExchangeRate> getCurrentRates() {
        if (ratesCache.isEmpty()) {
            return Flux.empty(); // Курсы загружаются только через update-rates
        }
        return Flux.fromIterable(ratesCache.values())
                .doOnSubscribe(s -> log.debug("Providing all current rates from cache"));
    }

    @Override
    public Mono<ExchangeRate> getRate(String fromCurrency, String toCurrency) {
        String normalizedFrom = fromCurrency.toUpperCase();
        String normalizedTo = toCurrency.toUpperCase();

        if (!availableCurrencies.contains(normalizedFrom) || !availableCurrencies.contains(normalizedTo)) {
            return Mono.error(new IllegalArgumentException("Unsupported currency: " + normalizedFrom + " or " + normalizedTo + ". Supported: " + String.join(", ", availableCurrencies)));
        }

        String cacheKey = generateCacheKey(normalizedFrom, normalizedTo);
        ExchangeRate cachedRate = ratesCache.get(cacheKey);

        if (cachedRate != null) {
            return Mono.just(cachedRate);
        }

        if (isSameCurrency(normalizedFrom, normalizedTo)) {
            return Mono.just(createUnitRate(normalizedFrom, normalizedTo))
                    .doOnSuccess(this::cacheRate);
        }

        return Mono.zip(getRate(normalizedFrom, "RUB"), getRate("RUB", normalizedTo))
                .map(tuple -> calculateCrossRate(normalizedFrom, normalizedTo, tuple.getT1(), tuple.getT2()))
                .doOnSuccess(this::cacheRate)
                .onErrorMap(e -> new RuntimeException("Failed to calculate cross rate through RUB", e));
    }

    @Override
    public Mono<BigDecimal> convert(String fromCurrency, String toCurrency, BigDecimal amount, OperationType type, UUID userId) {
        return getRate(fromCurrency, toCurrency)
                .flatMap(rate -> {
                    BigDecimal exchangeRate = (type == OperationType.BUY) ? rate.getBuyRate() : rate.getSellRate();
                    BigDecimal converted = calculateConversion(amount, exchangeRate);
                    return saveOperation(fromCurrency, toCurrency, amount, converted, exchangeRate, type, userId)
                            .thenReturn(converted);
                });
    }

    @Override
    public Flux<String> getAvailableCurrencies() {
        return Flux.fromIterable(availableCurrencies);
    }

    @Override
    public Mono<Void> updateRatesFromGenerator(Flux<ExchangeRate> rates) {
        return rates
                .doOnNext(this::cacheRate)
                .collectList()
                .doOnNext(rateList -> {
                    availableCurrencies = rateList.stream()
                            .flatMap(rate -> Stream.of(rate.getBaseCurrency(), rate.getTargetCurrency()))
                            .distinct()
                            .collect(Collectors.toList());
                    log.info("Updated rates from Generator: {} pairs, currencies: {}", rateList.size(), availableCurrencies);
                })
                .then();
    }

    private Mono<Void> saveOperation(String from, String to, BigDecimal amount, BigDecimal converted, BigDecimal rate, OperationType type, UUID userId) {
        Operation operation = Operation.builder()
                .userId(userId)
                .fromCurrency(from)
                .toCurrency(to)
                .amount(amount)
                .convertedAmount(converted)
                .exchangeRate(rate)
                .operationType(type)
                .createdAt(LocalDateTime.now())
                .build();
        return operationService.saveOperation(operation).then();
    }

    private ExchangeRate calculateCrossRate(String from, String to, ExchangeRate fromRub, ExchangeRate rubTo) {
        BigDecimal crossRate = fromRub.getBuyRate().multiply(rubTo.getBuyRate())
                .setScale(6, RoundingMode.HALF_UP);

        BigDecimal spreadAmount = crossRate.multiply(spread);

        return ExchangeRate.builder()
                .baseCurrency(from)
                .targetCurrency(to)
                .buyRate(crossRate.subtract(spreadAmount).setScale(scale, RoundingMode.HALF_UP))
                .sellRate(crossRate.add(spreadAmount).setScale(scale, RoundingMode.HALF_UP))
                .build();
    }

    private BigDecimal calculateConversion(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private void cacheRate(ExchangeRate rate) {
        String key = generateCacheKey(rate.getBaseCurrency(), rate.getTargetCurrency());
        ratesCache.put(key, rate);
    }

    private String generateCacheKey(String from, String to) {
        return from + "_" + to;
    }

    private boolean isSameCurrency(String from, String to) {
        return from.equals(to);
    }

    private ExchangeRate createUnitRate(String from, String to) {
        return ExchangeRate.builder()
                .baseCurrency(from)
                .targetCurrency(to)
                .buyRate(BigDecimal.ONE)
                .sellRate(BigDecimal.ONE)
                .build();
    }
}