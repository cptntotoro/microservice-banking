package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.AccountsServiceClient;
import ru.practicum.client.account.BalanceUpdateRequestDto;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.notification.NotificationRequestDto;
import ru.practicum.client.notification.NotificationsServiceClient;
import ru.practicum.dao.CashOperationDao;
import ru.practicum.model.CashRequest;
import ru.practicum.model.CashResponse;
import ru.practicum.repository.CashOperationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {
    /**
     * Клиент обращения к сервису аккаунтов
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
    public Mono<CashResponse> deposit(CashRequest request) {
        return processCashOperation(request, "DEPOSIT");
    }

    @Override
    @Transactional
    public Mono<CashResponse> withdraw(CashRequest request) {
        return processCashOperation(request, "WITHDRAWAL");
    }

    private Mono<CashResponse> processCashOperation(CashRequest request, String operationType) {
        UUID operationId = UUID.randomUUID();

        return validateRequest(request)
                .then(createOperationRecord(request, operationId, operationType))
                .flatMap(operation -> checkOperationBlocking(request, operationType))
                .flatMap(isBlocked -> handleBlockingResult(isBlocked, operationId))
                .flatMap(notBlocked -> executeFinancialOperation(request, operationId, operationType))
                .onErrorResume(this::handleOperationError);
    }

    private Mono<Void> validateRequest(CashRequest request) {
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

    private Mono<CashOperationDao> createOperationRecord(CashRequest request, UUID operationId, String operationType) {
        CashOperationDao operation = CashOperationDao.builder()
                .operationUuid(operationId)
                .accountId(request.getAccountId())
                .type(operationType)
                .amount(request.getAmount())
                .currencyCode(request.getCurrency())
                .status("PENDING")
                .description(String.format("%s %s %s",
                        operationType, request.getAmount(), request.getCurrency()))
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Начало операции {}: {}", operationType.toLowerCase(), operationId);

        return cashOperationRepository.save(operation);
    }

    private Mono<Boolean> checkOperationBlocking(CashRequest request, String operationType) {
        return blockerServiceClient.checkOperation(
                request.getAccountId(),
                request.getUserId(),
                request.getAmount(),
                request.getCurrency(),
                operationType
        ).doOnNext(blocked -> {
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

    private Mono<CashResponse> executeFinancialOperation(CashRequest request, UUID operationId, String operationType) {
        return verifyAccount(request.getAccountId(), request.getUserId())
                .flatMap(isValid -> handleAccountVerification(isValid, operationId))
                .flatMap(verified -> checkSufficientFunds(request, operationId, operationType))
                .flatMap(sufficient -> updateAccountBalance(request, operationType))
                .flatMap(updated -> sendNotification(request, operationType))
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
        if ("WITHDRAWAL".equals(operationType)) {
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
        boolean isDeposit = "DEPOSIT".equals(operationType);

        BalanceUpdateRequestDto updateRequest = BalanceUpdateRequestDto.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .isDeposit(isDeposit)
                .build();

        return accountsServiceClient.updateAccountBalance(updateRequest)
                .thenReturn(true);
    }

    private Mono<Boolean> sendNotification(CashRequest request, String operationType) {
        String action = "DEPOSIT".equals(operationType) ? "Пополнено" : "Снято";
        String message = String.format("%s %s %s со счета %s",
                action, request.getAmount(), request.getCurrency(), request.getAccountId());

        NotificationRequestDto notification = NotificationRequestDto.builder()
                .userId(request.getUserId())
                .message(message)
                .build();

        return notificationsServiceClient.sendNotification(notification)
                .thenReturn(true)
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