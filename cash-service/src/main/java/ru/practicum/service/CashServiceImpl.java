package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.AccountsServiceClient;
import ru.practicum.client.account.dto.BalanceUpdateRequestDto;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.blocker.dto.OperationCheckRequestDto;
import ru.practicum.client.notification.NotificationsServiceClient;
import ru.practicum.client.notification.dto.NotificationRequestDto;
import ru.practicum.dao.CashOperationDao;
import ru.practicum.dto.CashRequestDto;
import ru.practicum.model.CashRequest;
import ru.practicum.model.CashResponse;
import ru.practicum.repository.CashOperationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {
    private static final String DEPOSIT = "DEPOSIT";
    private static final String WITHDRAW = "WITHDRAW";

    /**
     * Клиент для обращений к сервису аккаунтов
     */
    private final AccountsServiceClient accountsServiceClient;

    /**
     * Клиент для взаимодействия с сервисом блокировки операций
     */
    private final BlockerServiceClient blockerServiceClient;

    /**
     * Клиент обращения к сервису оповещений
     */
    private final NotificationsServiceClient notificationsServiceClient;

    /**
     * Репозиторий операций
     */
    private final CashOperationRepository cashOperationRepository;

    @Override
    @Transactional
    public Mono<CashResponse> cashOperation(CashRequestDto request) {
        String operationType = request.getIsDeposit() ? DEPOSIT : WITHDRAW;

        return validateRequest(request)
                .then(createOperationRecord(request, operationType))
                .flatMap(operation -> checkOperationBlocking(request, operationType).zipWith(Mono.just(operation)))
                .flatMap(tuple2 -> handleBlockingResult(tuple2.getT1(), tuple2.getT2().getOperationUuid()).zipWith(Mono.just(tuple2.getT2())))
                .flatMap(tuple2 -> executeFinancialOperation(request, tuple2.getT2().getOperationUuid(), operationType))
                .onErrorResume(this::handleOperationError);
    }

    private Mono<Void> validateRequest(CashRequestDto request) {
        if (request.getAccountId() == null) {
            return Mono.error(new IllegalArgumentException("Требуется идентификатор счета"));
        }
        if (request.getUserId() == null) {
            return Mono.error(new IllegalArgumentException("Требуется идентификатор пользователя"));
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Сумма должна быть положительной"));
        }
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            return Mono.error(new IllegalArgumentException("Требуется валюта"));
        }
        return Mono.empty();
    }

    private Mono<CashOperationDao> createOperationRecord(CashRequestDto request, String operationType) {
        CashOperationDao operation = CashOperationDao.builder()
                .accountId(request.getAccountId())
                .operationType(operationType)
                .amount(request.getAmount().setScale(2, RoundingMode.HALF_DOWN))
                .currencyCode(request.getCurrency())
                .status("PENDING")
                .description(String.format("%s %s %s",
                        operationType, request.getAmount(), request.getCurrency()))
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Начало операции: {}", operation);

        return cashOperationRepository.save(operation)
                .doOnSuccess(saved -> log.info("Операция сохранена: {}", saved))
                .doOnError(error -> log.error("Ошибка сохранения операции: {}", error.getMessage()));
    }

    private Mono<Boolean> checkOperationBlocking(CashRequestDto request, String operationType) {

        OperationCheckRequestDto dto = OperationCheckRequestDto.builder()
                .operationId(UUID.randomUUID())
                .accountId(request.getAccountId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .operationType(operationType)
                .timestamp(LocalDateTime.now())
                .build();

        return blockerServiceClient.checkOperation(dto).doOnNext(blocked -> {
            if (Boolean.TRUE.equals(blocked)) {
                log.warn("Операция {} заблокирована службой безопасности", operationType);
            }
        });
    }

    private Mono<Boolean> handleBlockingResult(Boolean isBlocked, UUID operationId) {
        if (Boolean.TRUE.equals(isBlocked)) {
            return updateOperationStatus(operationId, "BLOCKED")
                    .then(Mono.error(new SecurityException("Операция заблокирована службой безопасности")));
        }
        return Mono.just(true);
    }

    private Mono<CashResponse> executeFinancialOperation(CashRequestDto request, UUID operationId, String operationType) {
        return checkAndUpdateAccountBalance(request)
                .flatMap(updated -> sendNotification(request))
                .flatMap(notified -> completeOperation(operationId, operationType))
                .onErrorResume(error -> handleExecutionError(error, operationId));
    }

    private Mono<Boolean> verifyAccount(UUID accountId, UUID userId) {
        return accountsServiceClient.verifyAccount(accountId, userId)
                .onErrorResume(e -> {
                    log.error("Ошибка проверки счета: {}", e.getMessage());
                    return Mono.just(false);
                });
    }

    private Mono<Boolean> handleAccountVerification(Boolean isValid, UUID operationId) {
        if (Boolean.FALSE.equals(isValid)) {
            return updateOperationStatus(operationId, "FAILED")
                    .then(Mono.error(new IllegalArgumentException("Неверный счет или пользователь")));
        }
        return Mono.just(true);
    }

    private Mono<Boolean> checkSufficientFunds(CashRequest request, UUID operationId, String operationType) {
        if ("WITHDRAW".equals(operationType)) {
            return accountsServiceClient.getAccountBalance(request.getAccountId())
                    .flatMap(balance -> {
                        if (balance.compareTo(request.getAmount()) < 0) {
                            return updateOperationStatus(operationId, "FAILED")
                                    .then(Mono.error(new IllegalStateException("Недостаточно средств")));
                        }
                        return Mono.just(true);
                    });
        }
        return Mono.just(true);
    }

    private Mono<Boolean> updateAccountBalance(CashRequest request, String operationType) {
        boolean isDeposit = DEPOSIT.equalsIgnoreCase(operationType);

        BalanceUpdateRequestDto updateRequest = BalanceUpdateRequestDto.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .isDeposit(isDeposit)
                .build();

        return accountsServiceClient.updateAccountBalance(updateRequest)
                .thenReturn(true);
    }

    private Mono<Boolean> checkAndUpdateAccountBalance(CashRequestDto request) {
        BalanceUpdateRequestDto updateRequest = BalanceUpdateRequestDto.builder()
                .userId(request.getUserId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .isDeposit(request.getIsDeposit())
                .build();

        return accountsServiceClient.checkAndUpdateAccountBalance(updateRequest);
    }

    private Mono<Boolean> sendNotification(CashRequestDto request) {
        String message = String.format("%s %s %s со счета %s",
                request.getIsDeposit() ? "Пополнено" : "Снято", request.getAmount(), request.getCurrency(), request.getAccountId());

        return accountsServiceClient.getUser(request.getUserId())
                .map(userResponseDto -> NotificationRequestDto.builder()
                        .email(userResponseDto.getEmail())
                        .title("Message from cash-service")
                        .description(message)
                        .build()
                )
                .flatMap(notificationRequestDto -> notificationsServiceClient.sendNotification(notificationRequestDto)
                        .thenReturn(true))
                .onErrorResume(e -> {
                    log.warn("Ошибка отправки уведомления: {}", e.getMessage());
                    return Mono.just(true); // Продолжаем даже если уведомление не отправлено
                });
    }

    private Mono<CashResponse> completeOperation(UUID operationId, String operationType) {
        return updateOperationStatus(operationId, "COMPLETED")
                .then(Mono.just(CashResponse.builder()
                        .status("SUCCESS")
                        .message(String.format("%s успешно завершен", operationType))
                        .build()))
                .doOnSuccess(response ->
                        log.info("Операция {} успешно завершена: {}", operationType, operationId));
    }

    private Mono<CashResponse> handleOperationError(Throwable error) {
        log.error("Ошибка обработки операции: {}", error.getMessage());

        String status = error instanceof SecurityException ? "BLOCKED" : "ERROR";
        String message = error.getMessage();

        return Mono.just(CashResponse.builder()
                .status(status)
                .message(message)
                .build());
    }

    private Mono<Void> updateOperationStatus(UUID operationId, String status) {
        return cashOperationRepository.findById(operationId)
                .flatMap(operation -> {
                    operation.setStatus(status);
                    operation.setCompletedAt(LocalDateTime.now());
                    return cashOperationRepository.save(operation);
                })
                .then();
    }

    private Mono<CashResponse> handleExecutionError(Throwable error, UUID operationId) {
        log.error("Ошибка выполнения для операции {}: {}", operationId, error.getMessage());

        return updateOperationStatus(operationId, "FAILED")
                .then(Mono.just(CashResponse.builder()
                        .status("ERROR")
                        .message(error.getMessage())
                        .build()));
    }
}