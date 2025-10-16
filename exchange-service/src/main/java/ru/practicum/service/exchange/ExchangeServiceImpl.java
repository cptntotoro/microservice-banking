package ru.practicum.service.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.exchange.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeServiceImpl implements ExchangeService {

    /**
     * In-memory кэш курсов относительно RUB
     */
    private final Map<String, ExchangeRate> rubRatesCache = new ConcurrentHashMap<>();

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
        if (rubRatesCache.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(rubRatesCache.values())
                .doOnSubscribe(s -> log.debug("Предоставление всех текущих курсов RUB из кэша"));
    }

    @Override
    public Mono<ExchangeRate> getRate(String fromCurrency, String toCurrency) {
        String normalizedFrom = fromCurrency.toUpperCase();
        String normalizedTo = toCurrency.toUpperCase();

        if (!availableCurrencies.contains(normalizedFrom) || !availableCurrencies.contains(normalizedTo)) {
            return Mono.error(new IllegalArgumentException("Неподдерживаемая валюта: " + normalizedFrom + " или " + normalizedTo + ". Поддерживаемые: " + String.join(", ", availableCurrencies)));
        }

        if (isSameCurrency(normalizedFrom, normalizedTo)) {
            return Mono.just(createUnitRate(normalizedFrom, normalizedTo));
        }

        if ("RUB".equals(normalizedTo)) {
            ExchangeRate rubRate = rubRatesCache.get(normalizedFrom);
            if (rubRate != null) {
                return Mono.just(rubRate);
            }
            return Mono.error(new IllegalArgumentException("Курс не найден для: " + normalizedFrom + "/RUB"));
        }

        if ("RUB".equals(normalizedFrom)) {
            ExchangeRate rubRate = rubRatesCache.get(normalizedTo);
            if (rubRate != null) {
                return Mono.just(invertRate(rubRate));
            }
            return Mono.error(new IllegalArgumentException("Курс не найден для: RUB/" + normalizedTo));
        }

        return calculateCrossRateThroughRUB(normalizedFrom, normalizedTo);
    }

    @Override
    public Mono<BigDecimal> convert(String fromCurrency, String toCurrency, BigDecimal amount) {
        String normalizedFrom = fromCurrency.toUpperCase();
        String normalizedTo = toCurrency.toUpperCase();
        ExchangeRate fromRate = rubRatesCache.get(normalizedFrom);
        ExchangeRate toRate = rubRatesCache.get(normalizedTo);
        return Mono.just(amount.multiply(fromRate.getBuyRate()).divide(toRate.getSellRate(), 2, RoundingMode.HALF_UP));
    }

    @Override
    public Flux<String> getAvailableCurrencies() {
        return Flux.fromIterable(availableCurrencies);
    }

    @Override
    public Mono<Void> updateRatesFromGenerator(Flux<ExchangeRate> rates) {
        return rates
                .doOnNext(rate -> {
                    if ("RUB".equals(rate.getTargetCurrency())) {
                        rubRatesCache.put(rate.getBaseCurrency(), rate);
                    }
                })
                .collectList()
                .doOnNext(rateList -> {
                    // Гарантируем, что RUB всегда будет в списке доступных валют
                    Set<String> currencies = rateList.stream()
                            .flatMap(rate -> Stream.of(rate.getBaseCurrency(), rate.getTargetCurrency()))
                            .collect(Collectors.toSet());
                    currencies.add("RUB"); // Всегда добавляем RUB
                    availableCurrencies = List.copyOf(currencies);
                    log.info("Обновлены курсы RUB из генератора: {} пар, валюты: {}",
                            rubRatesCache.size(), availableCurrencies);
                })
                .then();
    }

    private Mono<ExchangeRate> calculateCrossRateThroughRUB(String fromCurrency, String toCurrency) {
        return Mono.zip(
                getRate(fromCurrency, "RUB"),
                getRate(toCurrency, "RUB")
        ).map(tuple -> {
            ExchangeRate fromToRub = tuple.getT1();
            ExchangeRate toToRub = tuple.getT2();

            BigDecimal crossBuyRate = fromToRub.getBuyRate()
                    .divide(toToRub.getSellRate(), 6, RoundingMode.HALF_UP);
            BigDecimal crossSellRate = fromToRub.getSellRate()
                    .divide(toToRub.getBuyRate(), 6, RoundingMode.HALF_UP);

            // Добавляем спред к кросс-курсу
            BigDecimal spreadAmountBuy = crossBuyRate.multiply(spread);
            BigDecimal spreadAmountSell = crossSellRate.multiply(spread);

            return ExchangeRate.builder()
                    .baseCurrency(fromCurrency)
                    .targetCurrency(toCurrency)
                    .buyRate(crossBuyRate.subtract(spreadAmountBuy).setScale(scale, RoundingMode.HALF_UP))
                    .sellRate(crossSellRate.add(spreadAmountSell).setScale(scale, RoundingMode.HALF_UP))
                    .build();
        });
    }

    private ExchangeRate invertRate(ExchangeRate rate) {
        return ExchangeRate.builder()
                .baseCurrency("RUB")
                .targetCurrency(rate.getBaseCurrency())
                .buyRate(BigDecimal.ONE.divide(rate.getSellRate(), scale, RoundingMode.HALF_UP))
                .sellRate(BigDecimal.ONE.divide(rate.getBuyRate(), scale, RoundingMode.HALF_UP))
                .build();
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