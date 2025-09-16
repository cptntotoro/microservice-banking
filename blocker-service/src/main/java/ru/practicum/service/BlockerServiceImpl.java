package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.OperationRecordDao;
import ru.practicum.model.BlockReasonCode;
import ru.practicum.model.OperationCheckRequest;
import ru.practicum.model.OperationCheckResponse;
import ru.practicum.model.OperationHistory;
import ru.practicum.repository.OperationRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockerServiceImpl implements BlockerService {
    /**
     * Репозиторий операций
     */
    private final OperationRecordRepository repository;

    private static final int FREQUENCY_THRESHOLD = 5; // Максимум операций за 10 минут
    private static final int RISK_THRESHOLD = 150; // Порог блокировки
    private static final int DUPLICATE_RISK_SCORE = 100;
    private static final int AMOUNT_ANOMALY_RISK_SCORE = 80;
    private static final int UNUSUAL_TIME_RISK_SCORE = 70;
    private static final int HIGH_FREQUENCY_RISK_SCORE = 90;
    private static final int BLOCKED_HISTORY_RISK_SCORE = 50; // Дополнительный риск за историю блокировок

    @Override
    public Mono<OperationCheckResponse> checkOperation(OperationCheckRequest request) {
        log.info("Проверка операции: {} для пользователя: {}", request.getOperationType(), request.getUserId());

        validateRequest(request);

        return repository.existsByOperationId(request.getOperationId())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("Обнаружена дублирующаяся операция: {}", request.getOperationId());
                        return Mono.just(createBlockedResponse(
                                BlockReasonCode.DUPLICATE_OPERATION,
                                "Дублирующийся идентификатор операции: " + request.getOperationId(),
                                DUPLICATE_RISK_SCORE
                        ));
                    }
                    return performChecks(request);
                })
                .flatMap(response ->
                        saveOperationRecord(request, response)
                                .thenReturn(response)
                                .doOnSuccess(r -> logResult(request, r))
                );
    }

    @Override
    public Flux<OperationHistory> getOperationHistory(UUID userId) {
        log.info("Получение истории операций для пользователя: {}", userId);

        return repository.findByUserId(userId)
                .map(this::convertToOperationHistory);
    }


    private void validateRequest(OperationCheckRequest request) {
        if (request.getOperationId() == null) {
            throw new IllegalArgumentException("Идентификатор операции обязателен");
        }
        if (request.getOperationType() == null) {
            throw new IllegalArgumentException("Тип операции обязателен");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("Идентификатор пользователя обязателен");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        if (request.getCurrency() == null || request.getCurrency().length() != 3) {
            throw new IllegalArgumentException("Валюта должна быть 3-символьным кодом");
        }
        if (request.getTimestamp() == null) {
            throw new IllegalArgumentException("Время операции обязательно");
        }
    }

    private Mono<OperationCheckResponse> performChecks(OperationCheckRequest request) {
        return Mono.zip(
                checkAmountAnomaly(request),
                checkUnusualTime(request),
                checkFrequency(request),
                checkBlockedHistory(request) // Новая проверка истории блокировок
        ).map(results -> {
            int totalRiskScore = results.getT1().getRiskScore() +
                    results.getT2().getRiskScore() +
                    results.getT3().getRiskScore() +
                    results.getT4().getRiskScore();

            boolean shouldBlock = totalRiskScore > RISK_THRESHOLD;

            return OperationCheckResponse.builder()
                    .blocked(shouldBlock)
                    .reasonCode(shouldBlock ? BlockReasonCode.COMPOSITE_RISK : null)
                    .description(shouldBlock ? "Высокий совокупный риск: " + totalRiskScore + " баллов" : null)
                    .riskScore(totalRiskScore)
                    .build();
        });
    }

    private Mono<OperationCheckResponse> checkAmountAnomaly(OperationCheckRequest request) {
        return repository.findAverageAmountByUserAndType(
                        request.getUserId(),
                        request.getOperationType().name()
                )
                .defaultIfEmpty(0.0)
                .map(averageAmount -> {
                    if (averageAmount == 0.0) {
                        // Первая операция такого типа для пользователя
                        return OperationCheckResponse.builder()
                                .blocked(false)
                                .riskScore(10) // Базовый риск для первой операции
                                .build();
                    }

                    BigDecimal average = BigDecimal.valueOf(averageAmount);
                    BigDecimal currentAmount = request.getAmount();

                    // Если сумма превышает среднюю в 2 раза
                    boolean isAnomaly = currentAmount.compareTo(average.multiply(BigDecimal.valueOf(2))) > 0;

                    return OperationCheckResponse.builder()
                            .blocked(false)
                            .reasonCode(isAnomaly ? BlockReasonCode.AMOUNT_ANOMALY : null)
                            .riskScore(isAnomaly ? AMOUNT_ANOMALY_RISK_SCORE : 10)
                            .build();
                });
    }

    private Mono<OperationCheckResponse> checkUnusualTime(OperationCheckRequest request) {
        return Mono.fromCallable(() -> {
            LocalTime time = request.getTimestamp().toLocalTime();

            // Необычное время: с 23:00 до 6:00
            boolean isUnusualTime = time.isAfter(LocalTime.of(23, 0)) ||
                    time.isBefore(LocalTime.of(6, 0));

            return OperationCheckResponse.builder()
                    .blocked(false)
                    .reasonCode(isUnusualTime ? BlockReasonCode.UNUSUAL_TIME : null)
                    .riskScore(isUnusualTime ? UNUSUAL_TIME_RISK_SCORE : 5)
                    .build();
        });
    }

    private Mono<OperationCheckResponse> checkFrequency(OperationCheckRequest request) {
        LocalDateTime tenMinutesAgo = request.getTimestamp().minusMinutes(10);

        return repository.countOperationsByUserSince(request.getUserId(), tenMinutesAgo)
                .map(count -> {
                    boolean isFrequent = count > FREQUENCY_THRESHOLD;

                    return OperationCheckResponse.builder()
                            .blocked(false)
                            .reasonCode(isFrequent ? BlockReasonCode.HIGH_FREQUENCY : null)
                            .riskScore(isFrequent ? HIGH_FREQUENCY_RISK_SCORE : 0)
                            .build();
                });
    }

    private Mono<OperationCheckResponse> checkBlockedHistory(OperationCheckRequest request) {
        LocalDateTime twentyFourHoursAgo = request.getTimestamp().minusHours(24);

        return repository.countBlockedOperationsByUserSince(request.getUserId(), twentyFourHoursAgo)
                .map(blockedCount -> {
                    // Добавляем риск в зависимости от количества заблокированных операций за последние 24 часа
                    int riskFromHistory = Math.min(blockedCount * BLOCKED_HISTORY_RISK_SCORE, 200);

                    return OperationCheckResponse.builder()
                            .blocked(false)
                            .riskScore(riskFromHistory)
                            .build();
                });
    }

    private Mono<OperationRecordDao> saveOperationRecord(OperationCheckRequest request, OperationCheckResponse response) {
        OperationRecordDao record = OperationRecordDao.builder()
                .operationId(request.getOperationId())
                .operationType(request.getOperationType())
                .userId(request.getUserId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .timestamp(request.getTimestamp())
                .createdAt(LocalDateTime.now())
                .blocked(response.isBlocked())
                .blockReasonCode(response.isBlocked() && response.getReasonCode() != null ?
                        response.getReasonCode().name() : null)
                .riskScore(response.getRiskScore())
                .build();

        return repository.save(record);
    }

    private OperationCheckResponse createBlockedResponse(BlockReasonCode reasonCode, String description, int riskScore) {
        return OperationCheckResponse.builder()
                .blocked(true)
                .reasonCode(reasonCode)
                .description(description)
                .riskScore(riskScore)
                .build();
    }

    private void logResult(OperationCheckRequest request, OperationCheckResponse response) {
        if (response.isBlocked()) {
            log.warn("Операция {} заблокирована. Причина: {}, Риск: {}",
                    request.getOperationId(),
                    response.getReasonCode(),
                    response.getRiskScore());
        } else {
            log.info("Операция {} разрешена. Уровень риска: {}",
                    request.getOperationId(),
                    response.getRiskScore());
        }
    }

    private OperationHistory convertToOperationHistory(OperationRecordDao dao) {
        return OperationHistory.builder()
                .operationId(dao.getOperationId())
                .operationType(dao.getOperationType())
                .accountId(dao.getAccountId())
                .amount(dao.getAmount())
                .currency(dao.getCurrency())
                .timestamp(dao.getTimestamp())
                .blocked(dao.getBlocked())
                .blockReasonCode(dao.getBlockReasonCode())
                .riskScore(dao.getRiskScore())
                .build();
    }
}