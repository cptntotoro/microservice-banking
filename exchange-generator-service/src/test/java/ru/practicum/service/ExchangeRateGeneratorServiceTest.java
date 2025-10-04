//package ru.practicum.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//import ru.practicum.client.exchange.dto.ExchangeRateDto;
//import ru.practicum.client.exchange.ExchangeServiceClient;
//import ru.practicum.mapper.exchange.ExchangeRateMapper;
//import ru.practicum.model.exchange.ExchangeRate;
//import ru.practicum.service.exchange.ExchangeRateGeneratorServiceImpl;
//
//import java.lang.reflect.Method;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ExchangeRateGeneratorServiceTest {
//
//    @InjectMocks
//    private ExchangeRateGeneratorServiceImpl generatorService;
//
//    @Mock
//    private ExchangeServiceClient exchangeServiceClient;
//
//    @Mock
//    private ExchangeRateMapper exchangeRateMapper;
//
//    @BeforeEach
//    void setUp() {
//        when(exchangeServiceClient.sendExchangeRates(any())).thenReturn(Mono.empty());
//
//        when(exchangeRateMapper.exchangeRateToExchangeRateDto(any()))
//                .thenAnswer(invocation -> {
//                    ExchangeRate rate = invocation.getArgument(0);
//                    return ExchangeRateDto.builder()
//                            .baseCurrency(rate.getBaseCurrency())
//                            .targetCurrency(rate.getTargetCurrency())
//                            .buyRate(rate.getBuyRate())
//                            .sellRate(rate.getSellRate())
//                            .build();
//                });
//    }
//
//    @Test
//    void generateRates_shouldGenerateAllPairsAndSendToExchange() throws Exception {
//        // Вызываем приватный метод generateRates через рефлексию
//        Method generateRatesMethod = ExchangeRateGeneratorServiceImpl.class.getDeclaredMethod("generateRates");
//        generateRatesMethod.setAccessible(true);
//        generateRatesMethod.invoke(generatorService);
//
//        // Проверяем, что сгенерированы все пары (6 валют -> 36 пар)
//        Flux<ExchangeRate> result = generatorService.getCurrentRates();
//        StepVerifier.create(result)
//                .expectNextCount(36) // 6 * 6 пар
//                .verifyComplete();
//
//        // Проверяем, что sendExchangeRates вызван с 36 DTO
//        verify(exchangeServiceClient).sendExchangeRates(any(List.class));
//    }
//}