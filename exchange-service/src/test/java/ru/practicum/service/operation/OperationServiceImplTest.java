package ru.practicum.service.operation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.OperationDao;
import ru.practicum.mapper.operation.OperationMapper;
import ru.practicum.model.operation.Operation;
import ru.practicum.model.operation.OperationType;
import ru.practicum.repository.OperationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceImplTest {

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private OperationMapper operationMapper;

    @InjectMocks
    private OperationServiceImpl operationService;

    @Test
    void saveOperation_ShouldSaveAndReturnOperation() {
        Operation operation = createTestOperation();
        OperationDao operationDao = createTestOperationDao();
        when(operationMapper.operationToOperationDao(operation)).thenReturn(operationDao);
        when(operationRepository.save(operationDao)).thenReturn(Mono.just(operationDao));
        when(operationMapper.operationDaotoOperation(operationDao)).thenReturn(operation);

        StepVerifier.create(operationService.saveOperation(operation))
                .expectNext(operation)
                .verifyComplete();

        verify(operationRepository).save(operationDao);
    }

    @Test
    void getRecentOperations_ShouldReturnOperations() {
        OperationDao operationDao = createTestOperationDao();
        Operation operation = createTestOperation();
        when(operationRepository.findRecentOperations(10)).thenReturn(Flux.just(operationDao));
        when(operationMapper.operationDaotoOperation(operationDao)).thenReturn(operation);

        StepVerifier.create(operationService.getRecentOperations(10))
                .expectNext(operation)
                .verifyComplete();

        verify(operationRepository).findRecentOperations(10);
    }

    @Test
    void getOperationsByCurrencyPair_ShouldReturnOperations() {
        OperationDao operationDao = createTestOperationDao();
        Operation operation = createTestOperation();
        when(operationRepository.findByFromCurrencyAndToCurrency("USD", "RUB")).thenReturn(Flux.just(operationDao));
        when(operationMapper.operationDaotoOperation(operationDao)).thenReturn(operation);

        StepVerifier.create(operationService.getOperationsByCurrencyPair("USD", "RUB"))
                .expectNext(operation)
                .verifyComplete();

        verify(operationRepository).findByFromCurrencyAndToCurrency("USD", "RUB");
    }

    @Test
    void getAllUsedCurrencies_ShouldReturnCurrencies() {
        when(operationRepository.findAllUsedCurrencies()).thenReturn(Flux.just("USD", "EUR", "RUB"));

        StepVerifier.create(operationService.getAllUsedCurrencies())
                .expectNext("USD", "EUR", "RUB")
                .verifyComplete();

        verify(operationRepository).findAllUsedCurrencies();
    }

    private Operation createTestOperation() {
        return Operation.builder()
                .id(UUID.randomUUID())
                .fromCurrency("USD")
                .toCurrency("RUB")
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("7550.00"))
                .exchangeRate(new BigDecimal("75.50"))
                .operationType(OperationType.BUY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private OperationDao createTestOperationDao() {
        return OperationDao.builder()
                .id(UUID.randomUUID())
                .fromCurrency("USD")
                .toCurrency("RUB")
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("7550.00"))
                .exchangeRate(new BigDecimal("75.50"))
                .operationType(OperationType.BUY)
                .createdAt(LocalDateTime.now())
                .build();
    }
}