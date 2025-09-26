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
import ru.practicum.config.CurrencyConfig;
import ru.practicum.dao.currency.CurrencyDao;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.currency.CurrencyMapper;
import ru.practicum.model.currency.Currency;
import ru.practicum.repository.currency.CurrencyRepository;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyMapper currencyMapper;

    @Mock
    private CurrencyConfig currencyConfig;

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
                .name("US Dollar")
                .build();

        currencyDao = CurrencyDao.builder()
                .id(currencyId)
                .code(currencyCode)
                .name("US Dollar")
                .build();
    }

    @Test
    void createCurrency_shouldCreateSuccessfully() {
        when(currencyMapper.currencyToCurrencyDao(currency)).thenReturn(currencyDao);
        when(currencyRepository.save(currencyDao)).thenReturn(Mono.just(currencyDao));
        when(currencyMapper.currencyDaoToCurrency(currencyDao)).thenReturn(currency);

        StepVerifier.create(currencyService.createCurrency(currency))
                .expectNext(currency)
                .verifyComplete();

        verify(currencyRepository).save(currencyDao);
    }

    @Test
    void getCurrencyById_shouldReturnCurrency() {
        when(currencyRepository.findById(currencyId)).thenReturn(Mono.just(currencyDao));
        when(currencyMapper.currencyDaoToCurrency(currencyDao)).thenReturn(currency);

        StepVerifier.create(currencyService.getCurrencyById(currencyId))
                .expectNext(currency)
                .verifyComplete();
    }

    @Test
    void getCurrencyById_shouldThrowIfNotFound() {
        when(currencyRepository.findById(currencyId)).thenReturn(Mono.empty());

        StepVerifier.create(currencyService.getCurrencyById(currencyId))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getCurrencyByCode_shouldReturnCurrency() {
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Mono.just(currencyDao));
        when(currencyMapper.currencyDaoToCurrency(currencyDao)).thenReturn(currency);

        StepVerifier.create(currencyService.getCurrencyByCode(currencyCode))
                .expectNext(currency)
                .verifyComplete();
    }

    @Test
    void getCurrencyByCode_shouldThrowIfNotFound() {
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Mono.empty());

        StepVerifier.create(currencyService.getCurrencyByCode(currencyCode))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getAllCurrencies_shouldReturnFlux() {
        CurrencyDao dao1 = CurrencyDao.builder().id(UUID.randomUUID()).code("USD").name("US Dollar").build();
        CurrencyDao dao2 = CurrencyDao.builder().id(UUID.randomUUID()).code("EUR").name("Euro").build();
        Currency currency1 = Currency.builder().id(dao1.getId()).code("USD").name("US Dollar").build();
        Currency currency2 = Currency.builder().id(dao2.getId()).code("EUR").name("Euro").build();

        when(currencyRepository.findAll()).thenReturn(Flux.just(dao1, dao2));
        when(currencyMapper.currencyDaoToCurrency(dao1)).thenReturn(currency1);
        when(currencyMapper.currencyDaoToCurrency(dao2)).thenReturn(currency2);

        StepVerifier.create(currencyService.getAllCurrencies())
                .expectNext(currency1, currency2)
                .verifyComplete();
    }

    @Test
    void getAllCurrencies_shouldReturnEmptyIfNoCurrencies() {
        when(currencyRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(currencyService.getAllCurrencies())
                .expectComplete()
                .verify();
    }

    @Test
    void isValidCurrency_shouldReturnTrueIfCurrencyExists() {
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Mono.just(currencyDao));

        StepVerifier.create(currencyService.isValidCurrency(currencyCode))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isValidCurrency_shouldReturnFalseIfCurrencyNotExists() {
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Mono.empty());

        StepVerifier.create(currencyService.isValidCurrency(currencyCode))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getCurrencyName_shouldReturnCorrectName() {
        String result = currencyService.getCurrencyName("USD");
        assert result.equals("US Dollar");
    }

    @Test
    void getCurrencyName_shouldReturnCodeIfNameNotFound() {
        String result = currencyService.getCurrencyName("UNKNOWN");
        assert result.equals("UNKNOWN");
    }
}