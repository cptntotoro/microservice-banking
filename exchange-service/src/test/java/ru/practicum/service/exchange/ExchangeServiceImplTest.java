package ru.practicum.service.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.model.exchange.ExchangeRate;
import ru.practicum.model.operation.Operation;
import ru.practicum.model.operation.OperationType;
import ru.practicum.service.operation.OperationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceImplTest {

    @InjectMocks
    private ExchangeServiceImpl exchangeService;

    @Mock
    private OperationService operationService;

    private final BigDecimal spread = new BigDecimal("0.005");
    private final int scale = 4;

    @BeforeEach
    void setUp() {
        // Установка значений spread и scale через reflection
        try {
            var spreadField = ExchangeServiceImpl.class.getDeclaredField("spread");
            spreadField.setAccessible(true);
            spreadField.set(exchangeService, spread);
            var scaleField = ExchangeServiceImpl.class.getDeclaredField("scale");
            scaleField.setAccessible(true);
            scaleField.set(exchangeService, scale);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    void getCurrentRates_emptyCache_returnsEmptyFlux() {
        StepVerifier.create(exchangeService.getCurrentRates())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getCurrentRates_nonEmptyCache_returnsRates() {
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency("RUB")
                .targetCurrency("USD")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(rate)).block();

        StepVerifier.create(exchangeService.getCurrentRates())
                .expectNext(rate)
                .verifyComplete();
    }

    @Test
    void getRate_sameCurrency_returnsUnitRate() {
        exchangeService.updateRatesFromGenerator(Flux.just(
                ExchangeRate.builder().baseCurrency("USD").targetCurrency("RUB").build()
        )).block();

        StepVerifier.create(exchangeService.getRate("USD", "USD"))
                .expectNextMatches(rate ->
                        rate.getBaseCurrency().equals("USD") &&
                                rate.getTargetCurrency().equals("USD") &&
                                rate.getBuyRate().equals(BigDecimal.ONE) &&
                                rate.getSellRate().equals(BigDecimal.ONE))
                .verifyComplete();
    }

    @Test
    void getRate_cachedRate_returnsCached() {
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency("RUB")
                .targetCurrency("USD")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(rate)).block();

        StepVerifier.create(exchangeService.getRate("RUB", "USD"))
                .expectNext(rate)
                .verifyComplete();
    }

    @Test
    void getRate_crossRate_calculatesCorrectly() {
        ExchangeRate rubToUsd = ExchangeRate.builder()
                .baseCurrency("RUB")
                .targetCurrency("USD")
                .buyRate(new BigDecimal("0.013333")) // 1 RUB = 0.013333 USD
                .sellRate(new BigDecimal("0.013467"))
                .build();
        ExchangeRate eurToRub = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(rubToUsd, eurToRub)).block();

        StepVerifier.create(exchangeService.getRate("EUR", "USD"))
                .expectNextMatches(rate -> {
                    BigDecimal expectedCrossRate = new BigDecimal("75.0").multiply(new BigDecimal("0.013333")).setScale(6, RoundingMode.HALF_UP);
                    BigDecimal spreadAmount = expectedCrossRate.multiply(spread);
                    BigDecimal expectedBuy = expectedCrossRate.subtract(spreadAmount).setScale(scale, RoundingMode.HALF_UP);
                    BigDecimal expectedSell = expectedCrossRate.add(spreadAmount).setScale(scale, RoundingMode.HALF_UP);
                    return rate.getBaseCurrency().equals("EUR") &&
                            rate.getTargetCurrency().equals("USD") &&
                            rate.getBuyRate().equals(expectedBuy) &&
                            rate.getSellRate().equals(expectedSell);
                })
                .verifyComplete();
    }

    @Test
    void getRate_unsupportedCurrency_throwsIllegalArgumentException() {
        exchangeService.updateRatesFromGenerator(Flux.just(
                ExchangeRate.builder().baseCurrency("RUB").targetCurrency("USD").build()
        )).block();

        StepVerifier.create(exchangeService.getRate("EUR", "USD"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Unsupported currency: EUR"))
                .verify();
    }

    @Test
    void convert_buyOperation_savesOperationAndReturnsConvertedAmount() {
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        UUID userId = UUID.randomUUID();
        when(operationService.saveOperation(any())).thenReturn(Mono.just(new Operation()));

        exchangeService.updateRatesFromGenerator(Flux.just(rate)).block();

        StepVerifier.create(exchangeService.convert("USD", "RUB", new BigDecimal("100.0"), OperationType.BUY, userId))
                .expectNext(new BigDecimal("7500.00"))
                .verifyComplete();
    }

    @Test
    void convert_sellOperation_savesOperationAndReturnsConvertedAmount() {
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        UUID userId = UUID.randomUUID();
        when(operationService.saveOperation(any())).thenReturn(Mono.just(new Operation()));

        exchangeService.updateRatesFromGenerator(Flux.just(rate)).block();

        StepVerifier.create(exchangeService.convert("USD", "RUB", new BigDecimal("100.0"), OperationType.SELL, userId))
                .expectNext(new BigDecimal("7600.00"))
                .verifyComplete();
    }

    @Test
    void convert_unsupportedCurrency_throwsIllegalArgumentException() {
        exchangeService.updateRatesFromGenerator(Flux.just(
                ExchangeRate.builder().baseCurrency("RUB").targetCurrency("USD").build()
        )).block();

        StepVerifier.create(exchangeService.convert("EUR", "USD", new BigDecimal("100.0"), OperationType.BUY, UUID.randomUUID()))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Unsupported currency: EUR"))
                .verify();
    }

    @Test
    void getAvailableCurrencies_returnsCurrenciesFromCache() {
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency("RUB")
                .targetCurrency("USD")
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(rate)).block();

        StepVerifier.create(exchangeService.getAvailableCurrencies())
                .expectNext("RUB", "USD")
                .verifyComplete();
    }

    @Test
    void updateRatesFromGenerator_updatesCacheAndCurrencies() {
        ExchangeRate rate1 = ExchangeRate.builder()
                .baseCurrency("RUB")
                .targetCurrency("USD")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        ExchangeRate rate2 = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("90.0"))
                .sellRate(new BigDecimal("91.0"))
                .build();

        StepVerifier.create(exchangeService.updateRatesFromGenerator(Flux.just(rate1, rate2)))
                .verifyComplete();

        StepVerifier.create(exchangeService.getCurrentRates())
                .expectNextMatches(rate ->
                        (rate.getBaseCurrency().equals("RUB") && rate.getTargetCurrency().equals("USD") &&
                                rate.getBuyRate().equals(new BigDecimal("75.0")) && rate.getSellRate().equals(new BigDecimal("76.0"))) ||
                                (rate.getBaseCurrency().equals("EUR") && rate.getTargetCurrency().equals("RUB") &&
                                        rate.getBuyRate().equals(new BigDecimal("90.0")) && rate.getSellRate().equals(new BigDecimal("91.0"))))
                .expectNextMatches(rate ->
                        (rate.getBaseCurrency().equals("RUB") && rate.getTargetCurrency().equals("USD") &&
                                rate.getBuyRate().equals(new BigDecimal("75.0")) && rate.getSellRate().equals(new BigDecimal("76.0"))) ||
                                (rate.getBaseCurrency().equals("EUR") && rate.getTargetCurrency().equals("RUB") &&
                                        rate.getBuyRate().equals(new BigDecimal("90.0")) && rate.getSellRate().equals(new BigDecimal("91.0"))))
                .verifyComplete();

        StepVerifier.create(exchangeService.getAvailableCurrencies())
                .expectNext("RUB", "USD", "EUR")
                .verifyComplete();
    }

    @Test
    void updateRatesFromGenerator_withError_logsError() {
        Flux<ExchangeRate> errorFlux = Flux.error(new RuntimeException("Generator error"));

        StepVerifier.create(exchangeService.updateRatesFromGenerator(errorFlux))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Generator error"))
                .verify();
    }
}