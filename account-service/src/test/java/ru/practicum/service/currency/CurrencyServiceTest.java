package ru.practicum.service.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.client.ExchangeServiceClient;
import ru.practicum.dao.currency.CurrencyDao;
import ru.practicum.dto.exchange.AvailableCurrenciesDto;
import ru.practicum.exception.ErrorReasons;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.currency.CurrencyMapper;
import ru.practicum.model.currency.Currency;
import ru.practicum.repository.currency.CurrencyRepository;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyMapper currencyMapper;

    @Mock
    private ExchangeServiceClient exchangeServiceClient;

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    private Currency currency;
    private CurrencyDao currencyDao;
    private UUID currencyId;
    private String currencyCode;

    @BeforeEach
    void setUp() {
        currencyId = UUID.randomUUID();
        currencyCode = "USD";
        currency = Currency.builder()
                .id(currencyId)
                .code(currencyCode)
                .name(currencyCode)
                .build();

        currencyDao = CurrencyDao.builder()
                .id(currencyId)
                .code(currencyCode)
                .name(currencyCode)
                .build();
    }

    @Test
    void createCurrency_shouldCreateIfInitialized() {
        when(currencyRepository.count()).thenReturn(Mono.just(1L));
        when(currencyMapper.currencyToCurrencyDao(currency)).thenReturn(currencyDao);
        when(currencyRepository.save(currencyDao)).thenReturn(Mono.just(currencyDao));
        when(currencyMapper.currencyDaoToCurrency(currencyDao)).thenReturn(currency);

        StepVerifier.create(currencyService.createCurrency(currency))
                .expectNext(currency)
                .verifyComplete();

        verify(currencyRepository).save(currencyDao);
    }

    @Test
    void createCurrency_shouldThrowIfNotInitialized() {
        when(currencyRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(currencyService.createCurrency(currency))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Валюты не инициализированы. Пожалуйста, подождите или проверьте подключение к exchange-service.") &&
                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.CURRENCIES_NOT_INITIALIZED))
                .verify();
    }

    @Test
    void getCurrencyById_shouldReturnIfInitialized() {
        when(currencyRepository.count()).thenReturn(Mono.just(1L));
        when(currencyRepository.findById(currencyId)).thenReturn(Mono.just(currencyDao));
        when(currencyMapper.currencyDaoToCurrency(currencyDao)).thenReturn(currency);

        StepVerifier.create(currencyService.getCurrencyById(currencyId))
                .expectNext(currency)
                .verifyComplete();
    }

    @Test
    void getCurrencyById_shouldReturnEmptyIfNotFound() {
        when(currencyRepository.count()).thenReturn(Mono.just(1L));
        when(currencyRepository.findById(currencyId)).thenReturn(Mono.empty());

        StepVerifier.create(currencyService.getCurrencyById(currencyId))
                .expectComplete()
                .verify();
    }

    @Test
    void getCurrencyById_shouldThrowIfNotInitialized() {
        when(currencyRepository.count()).thenReturn(Mono.just(0L));
        when(currencyRepository.findById(any(UUID.class))).thenReturn(Mono.empty()); // Prevent NPE

        StepVerifier.create(currencyService.getCurrencyById(currencyId))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Валюты не инициализированы. Пожалуйста, подождите или проверьте подключение к exchange-service.") &&
                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.CURRENCIES_NOT_INITIALIZED))
                .verify();
    }

    @Test
    void getCurrencyByCode_shouldReturnIfInitialized() {
        when(currencyRepository.count()).thenReturn(Mono.just(1L));
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Mono.just(currencyDao));
        when(currencyMapper.currencyDaoToCurrency(currencyDao)).thenReturn(currency);

        StepVerifier.create(currencyService.getCurrencyByCode(currencyCode))
                .expectNext(currency)
                .verifyComplete();
    }

    @Test
    void getCurrencyByCode_shouldReturnEmptyIfNotFound() {
        when(currencyRepository.count()).thenReturn(Mono.just(1L));
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Mono.empty());

        StepVerifier.create(currencyService.getCurrencyByCode(currencyCode))
                .expectComplete()
                .verify();
    }

    @Test
    void getCurrencyByCode_shouldThrowIfNotInitialized() {
        when(currencyRepository.count()).thenReturn(Mono.just(0L));
        when(currencyRepository.findByCode(anyString())).thenReturn(Mono.empty()); // Prevent NPE

        StepVerifier.create(currencyService.getCurrencyByCode(currencyCode))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Валюты не инициализированы. Пожалуйста, подождите или проверьте подключение к exchange-service.") &&
                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.CURRENCIES_NOT_INITIALIZED))
                .verify();
    }

    @Test
    void getAllCurrencies_shouldReturnFluxIfInitialized() {
        CurrencyDao dao1 = CurrencyDao.builder().id(UUID.randomUUID()).code("USD").name("USD").build();
        CurrencyDao dao2 = CurrencyDao.builder().id(UUID.randomUUID()).code("EUR").name("EUR").build();
        Currency currency1 = Currency.builder().id(dao1.getId()).code("USD").name("USD").build();
        Currency currency2 = Currency.builder().id(dao2.getId()).code("EUR").name("EUR").build();

        when(currencyRepository.count()).thenReturn(Mono.just(2L));
        when(currencyRepository.findAll()).thenReturn(Flux.just(dao1, dao2));
        when(currencyMapper.currencyDaoToCurrency(dao1)).thenReturn(currency1);
        when(currencyMapper.currencyDaoToCurrency(dao2)).thenReturn(currency2);

        StepVerifier.create(currencyService.getAllCurrencies())
                .expectNext(currency1, currency2)
                .verifyComplete();
    }

    @Test
    void getAllCurrencies_shouldReturnEmptyIfNoCurrencies() {
        when(currencyRepository.count()).thenReturn(Mono.just(1L));
        when(currencyRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(currencyService.getAllCurrencies())
                .expectComplete()
                .verify();
    }

    @Test
    void getAllCurrencies_shouldThrowIfNotInitialized() {
        when(currencyRepository.count()).thenReturn(Mono.just(0L));
        when(currencyRepository.findAll()).thenReturn(Flux.empty()); // Prevent NPE

        StepVerifier.create(currencyService.getAllCurrencies())
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Валюты не инициализированы. Пожалуйста, подождите или проверьте подключение к exchange-service.") &&
                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.CURRENCIES_NOT_INITIALIZED))
                .verify();
    }

    @Test
    void initCurrencies_shouldCallCreateCurrencyIfNotExists() {
        AvailableCurrenciesDto dto = AvailableCurrenciesDto.builder()
                .currencies(List.of("USD", "EUR"))
                .build();

        when(exchangeServiceClient.getAvailableCurrencies()).thenReturn(Mono.just(dto));
        when(currencyRepository.count()).thenReturn(Mono.just(1L)); // Ensure count is mocked
        when(currencyRepository.findByCode("USD")).thenReturn(Mono.empty());
        when(currencyRepository.findByCode("EUR")).thenReturn(Mono.empty());
        when(currencyMapper.currencyToCurrencyDao(any(Currency.class))).thenReturn(currencyDao);
        when(currencyRepository.save(any(CurrencyDao.class))).thenReturn(Mono.just(currencyDao));
        when(currencyMapper.currencyDaoToCurrency(currencyDao)).thenReturn(currency);

        currencyService.initCurrencies();

        verify(exchangeServiceClient).getAvailableCurrencies();
        verify(currencyRepository, times(2)).findByCode(anyString());
        verify(currencyRepository, times(2)).save(any(CurrencyDao.class));
    }
}