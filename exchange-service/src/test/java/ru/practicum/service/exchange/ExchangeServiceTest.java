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
import ru.practicum.model.operation.OperationType;
import ru.practicum.service.operation.OperationService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

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
                .verifyComplete();
    }

    @Test
    void getCurrentRates_nonEmptyCache_returnsRates() {
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();

        exchangeService.updateRatesFromGenerator(Flux.just(rate)).block();

        StepVerifier.create(exchangeService.getCurrentRates())
                .expectNextMatches(actual ->
                        actual.getBaseCurrency().equals("USD") &&
                                actual.getTargetCurrency().equals("RUB") &&
                                actual.getBuyRate().equals(new BigDecimal("75.0")) &&
                                actual.getSellRate().equals(new BigDecimal("76.0")))
                .verifyComplete();
    }

    @Test
    void getRate_sameCurrency_returnsUnitRate() {
        ExchangeRate usdRate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(usdRate)).block();

        StepVerifier.create(exchangeService.getRate("USD", "USD"))
                .assertNext(rate -> {
                    assertEquals("USD", rate.getBaseCurrency());
                    assertEquals("USD", rate.getTargetCurrency());
                    assertEquals(BigDecimal.ONE, rate.getBuyRate());
                    assertEquals(BigDecimal.ONE, rate.getSellRate());
                })
                .verifyComplete();
    }

    @Test
    void getRate_directRateToRUB_returnsCached() {
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(rate)).block();

        StepVerifier.create(exchangeService.getRate("USD", "RUB"))
                .assertNext(actual -> {
                    assertEquals("USD", actual.getBaseCurrency());
                    assertEquals("RUB", actual.getTargetCurrency());
                    assertEquals(new BigDecimal("75.0"), actual.getBuyRate());
                    assertEquals(new BigDecimal("76.0"), actual.getSellRate());
                })
                .verifyComplete();
    }

    @Test
    void getRate_invertedRateFromRUB_returnsInverted() {
        ExchangeRate usdToRub = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(usdToRub)).block();

        StepVerifier.create(exchangeService.getRate("RUB", "USD"))
                .assertNext(rate -> {
                    assertEquals("RUB", rate.getBaseCurrency());
                    assertEquals("USD", rate.getTargetCurrency());
                    assertEquals(BigDecimal.ONE.divide(new BigDecimal("76.0"), scale, RoundingMode.HALF_UP),
                            rate.getBuyRate());
                    assertEquals(BigDecimal.ONE.divide(new BigDecimal("75.0"), scale, RoundingMode.HALF_UP),
                            rate.getSellRate());
                })
                .verifyComplete();
    }

    @Test
    void getRate_crossRate_calculatesCorrectly() {
        ExchangeRate usdToRub = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();

        ExchangeRate eurToRub = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("90.0"))
                .sellRate(new BigDecimal("91.0"))
                .build();

        exchangeService.updateRatesFromGenerator(Flux.just(usdToRub, eurToRub)).block();

        StepVerifier.create(exchangeService.getRate("EUR", "USD"))
                .assertNext(rate -> {
                    // EUR/USD = (EUR/RUB) / (USD/RUB)
                    // Buy: 90.0 / 76.0 = 1.1842105263
                    // Sell: 91.0 / 75.0 = 1.2133333333
                    BigDecimal baseBuyRate = new BigDecimal("90.0").divide(new BigDecimal("76.0"), 6, RoundingMode.HALF_UP);
                    BigDecimal baseSellRate = new BigDecimal("91.0").divide(new BigDecimal("75.0"), 6, RoundingMode.HALF_UP);

                    BigDecimal spreadAmountBuy = baseBuyRate.multiply(spread);
                    BigDecimal spreadAmountSell = baseSellRate.multiply(spread);

                    BigDecimal expectedBuyRate = baseBuyRate.subtract(spreadAmountBuy).setScale(scale, RoundingMode.HALF_UP);
                    BigDecimal expectedSellRate = baseSellRate.add(spreadAmountSell).setScale(scale, RoundingMode.HALF_UP);

                    assertEquals("EUR", rate.getBaseCurrency());
                    assertEquals("USD", rate.getTargetCurrency());
                    assertEquals(expectedBuyRate, rate.getBuyRate());
                    assertEquals(expectedSellRate, rate.getSellRate());
                })
                .verifyComplete();
    }

    @Test
    void getRate_unsupportedCurrency_throwsIllegalArgumentException() {
        ExchangeRate usdRate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        ExchangeRate eurRate = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("90.0"))
                .sellRate(new BigDecimal("91.0"))
                .build();

        exchangeService.updateRatesFromGenerator(Flux.just(usdRate, eurRate)).block();

        StepVerifier.create(exchangeService.getRate("GBP", "USD"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Неподдерживаемая валюта") &&
                        throwable.getMessage().contains("GBP") &&
                        throwable.getMessage().contains("Поддерживаемые: EUR, USD, RUB"))
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
        when(operationService.saveOperation(any())).thenReturn(Mono.empty());

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
        when(operationService.saveOperation(any())).thenReturn(Mono.empty());

        exchangeService.updateRatesFromGenerator(Flux.just(rate)).block();

        StepVerifier.create(exchangeService.convert("USD", "RUB", new BigDecimal("100.0"), OperationType.SELL, userId))
                .expectNext(new BigDecimal("7600.00"))
                .verifyComplete();
    }

    @Test
    void convert_crossCurrencyOperation_calculatesCorrectly() {
        ExchangeRate usdToRub = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();

        ExchangeRate eurToRub = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("90.0"))
                .sellRate(new BigDecimal("91.0"))
                .build();

        UUID userId = UUID.randomUUID();
        when(operationService.saveOperation(any())).thenReturn(Mono.empty());

        exchangeService.updateRatesFromGenerator(Flux.just(usdToRub, eurToRub)).block();

        StepVerifier.create(exchangeService.convert("EUR", "USD", new BigDecimal("100.0"), OperationType.BUY, userId))
                .assertNext(convertedAmount -> {
                    // Расчет: 100 EUR * (90.0 / 76.0) с учетом спреда
                    BigDecimal baseRate = new BigDecimal("90.0").divide(new BigDecimal("76.0"), 6, RoundingMode.HALF_UP);
                    BigDecimal spreadAmount = baseRate.multiply(spread);
                    BigDecimal finalRate = baseRate.subtract(spreadAmount).setScale(scale, RoundingMode.HALF_UP);
                    BigDecimal expectedAmount = new BigDecimal("100.0").multiply(finalRate).setScale(2, RoundingMode.HALF_UP);

                    assertEquals(expectedAmount, convertedAmount);
                })
                .verifyComplete();
    }

    @Test
    void convert_unsupportedCurrency_throwsIllegalArgumentException() {
        ExchangeRate usdRate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        ExchangeRate eurRate = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("90.0"))
                .sellRate(new BigDecimal("91.0"))
                .build();

        exchangeService.updateRatesFromGenerator(Flux.just(usdRate, eurRate)).block();

        StepVerifier.create(exchangeService.convert("GBP", "USD", new BigDecimal("100.0"), OperationType.BUY, UUID.randomUUID()))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Неподдерживаемая валюта") &&
                        throwable.getMessage().contains("GBP") &&
                        throwable.getMessage().contains("Поддерживаемые: EUR, USD, RUB"))
                .verify();
    }

    @Test
    void getAvailableCurrencies_returnsCurrenciesFromCache() throws Exception {
        ExchangeRate usdRate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        ExchangeRate eurRate = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("90.0"))
                .sellRate(new BigDecimal("91.0"))
                .build();

        exchangeService.updateRatesFromGenerator(Flux.just(usdRate, eurRate)).block();

        // Проверяем содержимое без привязки к порядку
        StepVerifier.create(exchangeService.getAvailableCurrencies())
                .expectNextCount(3)
                .thenConsumeWhile(currency -> true)
                .expectComplete()
                .verify();

        // дополнительная проверка через рефлексию
        Field currenciesField = ExchangeServiceImpl.class.getDeclaredField("availableCurrencies");
        currenciesField.setAccessible(true);
        var currencies = (List<String>) currenciesField.get(exchangeService);

        assertEquals(3, currencies.size());
        assertTrue(currencies.contains("USD"));
        assertTrue(currencies.contains("EUR"));
        assertTrue(currencies.contains("RUB"));
    }

    @Test
    void updateRatesFromGenerator_updatesCacheAndCurrencies() throws Exception {
        ExchangeRate usdRate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        ExchangeRate eurRate = ExchangeRate.builder()
                .baseCurrency("EUR")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("90.0"))
                .sellRate(new BigDecimal("91.0"))
                .build();

        StepVerifier.create(exchangeService.updateRatesFromGenerator(Flux.just(usdRate, eurRate)))
                .verifyComplete();

        // Проверяем кэш через рефлексию
        Field cacheField = ExchangeServiceImpl.class.getDeclaredField("rubRatesCache");
        cacheField.setAccessible(true);
        var cache = (java.util.Map<String, ExchangeRate>) cacheField.get(exchangeService);

        assertEquals(2, cache.size());
        assertTrue(cache.containsKey("USD"));
        assertTrue(cache.containsKey("EUR"));

        // Проверяем доступные валюты
        Field currenciesField = ExchangeServiceImpl.class.getDeclaredField("availableCurrencies");
        currenciesField.setAccessible(true);
        var currencies = (List<String>) currenciesField.get(exchangeService);

        assertEquals(3, currencies.size());
        assertTrue(currencies.contains("USD"));
        assertTrue(currencies.contains("EUR"));
        assertTrue(currencies.contains("RUB"));
    }

    @Test
    void updateRatesFromGenerator_withError_logsError() {
        Flux<ExchangeRate> errorFlux = Flux.error(new RuntimeException("Generator error"));

        StepVerifier.create(exchangeService.updateRatesFromGenerator(errorFlux))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getRate_rateNotFound_throwsIllegalArgumentException() throws Exception {
        ExchangeRate usdRate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(usdRate)).block();

        // Сначала проверяем, что EUR не поддерживается
        StepVerifier.create(exchangeService.getRate("EUR", "RUB"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Неподдерживаемая валюта") &&
                        throwable.getMessage().contains("EUR") &&
                        throwable.getMessage().contains("Поддерживаемые: USD, RUB"))
                .verify();

        // Для проверки "Курс не найден" добавляем EUR в availableCurrencies через рефлексию
        Field currenciesField = ExchangeServiceImpl.class.getDeclaredField("availableCurrencies");
        currenciesField.setAccessible(true);
        var currentCurrencies = (List<String>) currenciesField.get(exchangeService);
        List<String> updatedCurrencies = new java.util.ArrayList<>(currentCurrencies);
        updatedCurrencies.add("EUR");

        currenciesField.set(exchangeService, List.copyOf(updatedCurrencies));

        // Теперь EUR поддерживается, но курса нет в кэше
        StepVerifier.create(exchangeService.getRate("EUR", "RUB"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Курс не найден для: EUR/RUB"))
                .verify();
    }

    @Test
    void convert_rubToRub_returnsCorrectAmount() {
        ExchangeRate usdRate = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("75.0"))
                .sellRate(new BigDecimal("76.0"))
                .build();
        exchangeService.updateRatesFromGenerator(Flux.just(usdRate)).block();

        UUID userId = UUID.randomUUID();
        when(operationService.saveOperation(any())).thenReturn(Mono.empty());

        // RUB to RUB = 1:1
        StepVerifier.create(exchangeService.convert("RUB", "RUB", new BigDecimal("1000.0"), OperationType.BUY, userId))
                .expectNext(new BigDecimal("1000.00"))
                .verifyComplete();
    }
}