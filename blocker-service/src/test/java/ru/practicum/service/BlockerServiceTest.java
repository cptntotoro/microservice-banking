package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.OperationRecordDao;
import ru.practicum.model.BlockReasonCode;
import ru.practicum.model.OperationCheckRequest;
import ru.practicum.model.OperationCheckResponse;
import ru.practicum.model.OperationHistory;
import ru.practicum.model.OperationType;
import ru.practicum.repository.OperationRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockerServiceTest {

    @Mock
    private OperationRecordRepository repository;

    @InjectMocks
    private BlockerServiceImpl blockerService;

    private OperationCheckRequest validRequest;
    private final UUID operationId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        validRequest = OperationCheckRequest.builder()
                .operationId(operationId)
                .operationType(OperationType.DEPOSIT)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("1000.00"))
                .currency("RUB")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void checkOperation_shouldBlockDuplicateOperation() {
        when(repository.existsByOperationId(operationId)).thenReturn(Mono.just(true));
        when(repository.save(any(OperationRecordDao.class))).thenReturn(Mono.just(new OperationRecordDao()));

        Mono<OperationCheckResponse> result = blockerService.checkOperation(validRequest);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.isBlocked() &&
                                response.getReasonCode() == BlockReasonCode.DUPLICATE_OPERATION &&
                                response.getRiskScore() == 100
                )
                .verifyComplete();

        verify(repository).save(any(OperationRecordDao.class)); // Теперь сохраняются и заблокированные операции
    }

    @Test
    void checkOperation_shouldAllowNormalOperation() {
        when(repository.existsByOperationId(operationId)).thenReturn(Mono.just(false));
        when(repository.findAverageAmountByUserAndType(eq(userId), eq("DEPOSIT"))).thenReturn(Mono.just(500.0));
        when(repository.countOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(2));
        when(repository.countBlockedOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(0));
        when(repository.save(any(OperationRecordDao.class))).thenReturn(Mono.just(new OperationRecordDao()));

        Mono<OperationCheckResponse> result = blockerService.checkOperation(validRequest);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        !response.isBlocked() &&
                                response.getRiskScore() < 150
                )
                .verifyComplete();

        verify(repository).save(any(OperationRecordDao.class));
    }

    @Test
    void checkOperation_shouldBlockHighRiskOperation() {
        OperationCheckRequest riskyRequest = OperationCheckRequest.builder()
                .operationId(operationId)
                .operationType(OperationType.DEPOSIT)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("1500.00")) // Аномальная сумма
                .currency("RUB")
                .timestamp(LocalDateTime.now().with(LocalTime.of(3, 0))) // Ночное время
                .build();

        when(repository.existsByOperationId(operationId)).thenReturn(Mono.just(false));
        when(repository.findAverageAmountByUserAndType(eq(userId), eq("DEPOSIT"))).thenReturn(Mono.just(500.0));
        when(repository.countOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(6)); // Частые операции
        when(repository.countBlockedOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(0));
        when(repository.save(any(OperationRecordDao.class))).thenReturn(Mono.just(new OperationRecordDao()));

        Mono<OperationCheckResponse> result = blockerService.checkOperation(riskyRequest);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.isBlocked() &&
                                response.getReasonCode() == BlockReasonCode.COMPOSITE_RISK &&
                                response.getRiskScore() > 150
                )
                .verifyComplete();

        verify(repository).save(any(OperationRecordDao.class)); // Заблокированные операции тоже сохраняются
    }

    @Test
    void checkOperation_shouldHandleFirstTimeOperation() {
        when(repository.existsByOperationId(operationId)).thenReturn(Mono.just(false));
        when(repository.findAverageAmountByUserAndType(eq(userId), eq("DEPOSIT"))).thenReturn(Mono.just(0.0));
        when(repository.countOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(1));
        when(repository.countBlockedOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(0));
        when(repository.save(any(OperationRecordDao.class))).thenReturn(Mono.just(new OperationRecordDao()));

        Mono<OperationCheckResponse> result = blockerService.checkOperation(validRequest);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        !response.isBlocked() &&
                                response.getRiskScore() == 15 // 10 (amount) + 5 (time) + 0 (frequency) + 0 (history)
                )
                .verifyComplete();
    }

    @Test
    void checkOperation_shouldIncreaseRiskForBlockedHistory() {
        when(repository.existsByOperationId(operationId)).thenReturn(Mono.just(false));
        when(repository.findAverageAmountByUserAndType(eq(userId), eq("DEPOSIT"))).thenReturn(Mono.just(1000.0));
        when(repository.countOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(1));
        when(repository.countBlockedOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(2)); // 2 заблокированные операции
        when(repository.save(any(OperationRecordDao.class))).thenReturn(Mono.just(new OperationRecordDao()));

        Mono<OperationCheckResponse> result = blockerService.checkOperation(validRequest);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        !response.isBlocked() &&
                                response.getRiskScore() >= 100 // 10 + 5 + 0 + (2*50) = 115
                )
                .verifyComplete();
    }

    @Test
    void checkOperation_shouldValidateRequest() {
        OperationCheckRequest invalidRequest = OperationCheckRequest.builder()
                .operationId(null) // Невалидный ID
                .operationType(OperationType.DEPOSIT)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("1000.00"))
                .currency("RUB")
                .timestamp(LocalDateTime.now())
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            blockerService.checkOperation(invalidRequest).block();
        });
    }

    @Test
    void checkAmountAnomaly_shouldDetectAnomaly() {
        when(repository.existsByOperationId(operationId)).thenReturn(Mono.just(false));
        when(repository.findAverageAmountByUserAndType(eq(userId), eq("DEPOSIT"))).thenReturn(Mono.just(500.0));
        when(repository.countOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(1));
        when(repository.countBlockedOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(0));
        when(repository.save(any(OperationRecordDao.class))).thenReturn(Mono.just(new OperationRecordDao()));

        OperationCheckRequest request = OperationCheckRequest.builder()
                .operationId(operationId)
                .operationType(OperationType.DEPOSIT)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("1500.00")) // > 2 * 500
                .currency("RUB")
                .timestamp(LocalDateTime.now())
                .build();

        Mono<OperationCheckResponse> result = blockerService.checkOperation(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getRiskScore() >= 80)
                .verifyComplete();
    }

    @Test
    void checkUnusualTime_shouldDetectNightTime() {
        OperationCheckRequest nightRequest = OperationCheckRequest.builder()
                .operationId(operationId)
                .operationType(OperationType.DEPOSIT)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("1000.00"))
                .currency("RUB")
                .timestamp(LocalDateTime.now().with(LocalTime.of(2, 0)))
                .build();

        when(repository.existsByOperationId(operationId)).thenReturn(Mono.just(false));
        when(repository.findAverageAmountByUserAndType(eq(userId), eq("DEPOSIT"))).thenReturn(Mono.just(1000.0));
        when(repository.countOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(1));
        when(repository.countBlockedOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(0));
        when(repository.save(any(OperationRecordDao.class))).thenReturn(Mono.just(new OperationRecordDao()));

        Mono<OperationCheckResponse> result = blockerService.checkOperation(nightRequest);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getRiskScore() >= 70)
                .verifyComplete();
    }

    @Test
    void checkFrequency_shouldDetectHighFrequency() {
        when(repository.existsByOperationId(operationId)).thenReturn(Mono.just(false));
        when(repository.findAverageAmountByUserAndType(eq(userId), eq("DEPOSIT"))).thenReturn(Mono.just(1000.0));
        when(repository.countOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(10)); // Высокая частота
        when(repository.countBlockedOperationsByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(Mono.just(0));
        when(repository.save(any(OperationRecordDao.class))).thenReturn(Mono.just(new OperationRecordDao()));

        Mono<OperationCheckResponse> result = blockerService.checkOperation(validRequest);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getRiskScore() >= 90)
                .verifyComplete();
    }

    @Test
    void checkOperation_shouldHandleRepositoryErrors() {
        when(repository.existsByOperationId(operationId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(blockerService.checkOperation(validRequest))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getOperationHistory_shouldReturnUserOperations() {
        OperationRecordDao operation1 = OperationRecordDao.builder()
                .operationId(UUID.randomUUID())
                .operationType(OperationType.DEPOSIT)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("1000.00"))
                .currency("RUB")
                .timestamp(LocalDateTime.now().minusHours(1))
                .blocked(false)
                .riskScore(15)
                .build();

        OperationRecordDao operation2 = OperationRecordDao.builder()
                .operationId(UUID.randomUUID())
                .operationType(OperationType.WITHDRAWAL)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("500.00"))
                .currency("RUB")
                .timestamp(LocalDateTime.now())
                .blocked(true)
                .blockReasonCode("HIGH_FREQUENCY")
                .riskScore(160)
                .build();

        when(repository.findByUserId(userId)).thenReturn(Flux.just(operation1, operation2));

        Flux<OperationHistory> result = blockerService.getOperationHistory(userId);

        StepVerifier.create(result)
                .expectNextMatches(history ->
                        history.getOperationId().equals(operation1.getOperationId()) &&
                                history.getOperationType() == OperationType.DEPOSIT &&
                                history.getAmount().equals(new BigDecimal("1000.00")) &&
                                !history.getBlocked() &&
                                history.getRiskScore() == 15
                )
                .expectNextMatches(history ->
                        history.getOperationId().equals(operation2.getOperationId()) &&
                                history.getOperationType() == OperationType.WITHDRAWAL &&
                                history.getAmount().equals(new BigDecimal("500.00")) &&
                                history.getBlocked() &&
                                history.getBlockReasonCode().equals("HIGH_FREQUENCY") &&
                                history.getRiskScore() == 160
                )
                .verifyComplete();

        verify(repository).findByUserId(userId);
    }

    @Test
    void getOperationHistory_shouldReturnEmptyForUserWithoutOperations() {
        UUID emptyUserId = UUID.randomUUID();
        when(repository.findByUserId(emptyUserId)).thenReturn(Flux.empty());

        Flux<OperationHistory> result = blockerService.getOperationHistory(emptyUserId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(repository).findByUserId(emptyUserId);
    }

    @Test
    void getOperationHistory_shouldHandleRepositoryErrors() {
        when(repository.findByUserId(userId)).thenReturn(Flux.error(new RuntimeException("DB error")));

        Flux<OperationHistory> result = blockerService.getOperationHistory(userId);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(repository).findByUserId(userId);
    }

    @Test
    void getOperationHistory_shouldConvertAllFieldsCorrectly() {
        OperationRecordDao dao = OperationRecordDao.builder()
                .operationId(operationId)
                .operationType(OperationType.TRANSFER)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("750.50"))
                .currency("USD")
                .timestamp(LocalDateTime.now().minusMinutes(30))
                .createdAt(LocalDateTime.now())
                .blocked(false)
                .blockReasonCode(null)
                .riskScore(25)
                .build();

        when(repository.findByUserId(userId)).thenReturn(Flux.just(dao));

        Flux<OperationHistory> result = blockerService.getOperationHistory(userId);

        StepVerifier.create(result)
                .expectNextMatches(history ->
                        history.getOperationId().equals(operationId) &&
                                history.getOperationType() == OperationType.TRANSFER &&
                                history.getAccountId().equals(accountId) &&
                                history.getAmount().equals(new BigDecimal("750.50")) &&
                                history.getCurrency().equals("USD") &&
                                history.getTimestamp().equals(dao.getTimestamp()) &&
                                !history.getBlocked() &&
                                history.getBlockReasonCode() == null &&
                                history.getRiskScore() == 25
                )
                .verifyComplete();

        verify(repository).findByUserId(userId);
    }
}