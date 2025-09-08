package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.model.ExchangeRate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExchangeRateGeneratorServiceTest {

    @InjectMocks
    private ExchangeRateGeneratorServiceImpl generatorService;

    @Test
    void getCurrentRates_shouldReturnFluxOfExchangeRates() {
        ExchangeRate rubToUsd = ExchangeRate.builder()
                .baseCurrency("RUB")
                .targetCurrency("USD")
                .buyRate(new BigDecimal("75.00"))
                .sellRate(new BigDecimal("75.50"))
                .build();

        ExchangeRate usdToRub = ExchangeRate.builder()
                .baseCurrency("USD")
                .targetCurrency("RUB")
                .buyRate(new BigDecimal("0.0132"))
                .sellRate(new BigDecimal("0.0133"))
                .build();

        try {
            Field currentRatesField = ExchangeRateGeneratorServiceImpl.class.getDeclaredField("currentRates");
            currentRatesField.setAccessible(true);
            Map<String, ExchangeRate> currentRates = (Map<String, ExchangeRate>) currentRatesField.get(generatorService);

            currentRates.put("RUB_USD", rubToUsd);
            currentRates.put("USD_RUB", usdToRub);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access currentRates field", e);
        }

        Flux<ExchangeRate> result = generatorService.getCurrentRates();

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getRate_withExistingRate_shouldReturnMonoOfExchangeRate() {
        Mono<ExchangeRate> result = generatorService.getRate("RUB", "USD");

        StepVerifier.create(result)
                .assertNext(rate -> {
                    assertThat(rate.getBaseCurrency()).isEqualTo("RUB");
                    assertThat(rate.getTargetCurrency()).isEqualTo("USD");
                    assertThat(rate.getBuyRate()).isPositive();
                    assertThat(rate.getSellRate()).isPositive();
                    assertThat(rate.getSellRate()).isGreaterThan(rate.getBuyRate());
                })
                .verifyComplete();
    }

    @Test
    void getRate_withNonExistingRate_shouldGenerateAndReturnRate() {
        Mono<ExchangeRate> result = generatorService.getRate("EUR", "GBP");

        StepVerifier.create(result)
                .assertNext(rate -> {
                    assertThat(rate.getBaseCurrency()).isEqualTo("EUR");
                    assertThat(rate.getTargetCurrency()).isEqualTo("GBP");
                    assertThat(rate.getBuyRate()).isPositive();
                    assertThat(rate.getSellRate()).isPositive();
                })
                .verifyComplete();
    }

    @Test
    void getAvailableCurrencies_shouldReturnFluxOfCurrencyCodes() {
        Flux<String> result = generatorService.getAvailableCurrencies();

        StepVerifier.create(result)
                .expectNext("RUB", "USD", "EUR", "CNY", "GBP", "JPY")
                .verifyComplete();
    }
}