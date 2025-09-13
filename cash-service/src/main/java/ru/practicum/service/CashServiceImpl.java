package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
     * Клиент обращения к сервису блокировки подозрительных операций
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
    public Mono<CashResponse> deposit(CashRequest request) {
        UUID operationUuid = UUID.randomUUID();
        UUID accountId = request.getAccountId();
        UUID userId = request.getUserId();

        CashOperationDao operation = CashOperationDao.builder()
                .operationUuid(operationUuid)
                .accountId(accountId)
                .type("DEPOSIT")
                .amount(request.getAmount())
                .currencyCode(request.getCurrency())
                .status("PENDING")
                .description("Пополнение на сумму " + request.getAmount() + " " + request.getCurrency())
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Начинается операция пополнения: {}", operationUuid);

        return cashOperationRepository.save(operation)
                .then(validateRequest(request))
                .then(blockerServiceClient.isOperationBlocked(request))
                .flatMap(isBlocked -> {
                    if (Boolean.TRUE.equals(isBlocked)) {
                        log.warn("Операция заблокирована службой безопасности: {}", operationUuid);
                        return updateOperationStatus(operationUuid, "FAILED")
                                .then(Mono.just(CashResponse.builder()
                                        .status("BLOCKED")
                                        .message("Операция заблокирована службой безопасности")
                                        .build()));
                    }
                    return accountsServiceClient.verifyAccount(accountId, userId)
                            .flatMap(isValid -> {
                                if (Boolean.FALSE.equals(isValid)) {
                                    log.error("Неверный аккаунт или пользователь: accountId={}, userId={}",
                                            accountId, userId);
                                    return updateOperationStatus(operationUuid, "FAILED")
                                            .then(Mono.just(CashResponse.builder()
                                                    .status("ERROR")
                                                    .message("Неверный аккаунт или пользователь")
                                                    .build()));
                                }
                                return accountsServiceClient.updateAccountBalance(BalanceUpdateRequestDto.builder()
                                                .accountId(accountId)
                                                .amount(request.getAmount())
                                                .isDeposit(true)
                                                .build())
                                        .then(notificationsServiceClient.sendNotification(NotificationRequestDto.builder()
                                                .userId(request.getUserId())
                                                .message("Пополнено " + request.getAmount() + " " + request.getCurrency() + " на счет " + request.getAccountId())
                                                .build()))
                                        .then(updateOperationStatus(operationUuid, "COMPLETED"))
                                        .thenReturn(CashResponse.builder()
                                                .status("SUCCESS")
                                                .message("Пополнение успешно выполнено")
                                                .build())
                                        .doOnSuccess(response ->
                                                log.info("Операция пополнения успешно завершена: {}", operationUuid))
                                        .onErrorResume(e -> {
                                            log.error("Ошибка при пополнении: {}", e.getMessage(), e);
                                            return updateOperationStatus(operationUuid, "FAILED")
                                                    .then(Mono.error(e));
                                        });
                            });
                })
                .onErrorResume(e -> {
                    log.error("Ошибка в процессе пополнения: {}", e.getMessage(), e);
                    return updateOperationStatus(operationUuid, "FAILED")
                            .then(Mono.just(CashResponse.builder()
                                    .status("ERROR")
                                    .message(e.getMessage())
                                    .build()));
                });
    }

    @Override
    public Mono<CashResponse> withdraw(CashRequest request) {
        UUID operationUuid = UUID.randomUUID();
        UUID accountId = request.getAccountId();
        UUID userId = request.getUserId();

        CashOperationDao operation = CashOperationDao.builder()
                .operationUuid(operationUuid)
                .accountId(accountId)
                .type("WITHDRAWAL")
                .amount(request.getAmount())
                .currencyCode(request.getCurrency())
                .status("PENDING")
                .description("Снятие суммы " + request.getAmount() + " " + request.getCurrency())
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Начинается операция снятия: {}", operationUuid);

        return cashOperationRepository.save(operation)
                .then(validateRequest(request))
                .then(blockerServiceClient.isOperationBlocked(request))
                .flatMap(isBlocked -> {
                    if (Boolean.TRUE.equals(isBlocked)) {
                        log.warn("Операция снятия заблокирована службой безопасности: {}", operationUuid);
                        return updateOperationStatus(operationUuid, "FAILED")
                                .then(Mono.just(CashResponse.builder()
                                        .status("BLOCKED")
                                        .message("Операция заблокирована службой безопасности")
                                        .build()));
                    }
                    return accountsServiceClient.verifyAccount(accountId, userId)
                            .flatMap(isValid -> {
                                if (Boolean.FALSE.equals(isValid)) {
                                    log.error("Неверный аккаунт или пользователь: accountId={}, userId={}",
                                            accountId, userId);
                                    return updateOperationStatus(operationUuid, "FAILED")
                                            .then(Mono.just(CashResponse.builder()
                                                    .status("ERROR")
                                                    .message("Неверный аккаунт или пользователь")
                                                    .build()));
                                }
                                return accountsServiceClient.getAccountBalance(accountId)
                                        .flatMap(balance -> {
                                            if (balance.compareTo(request.getAmount()) < 0) {
                                                log.warn("Недостаточно средств: accountId={}, баланс={}, запрошено={}",
                                                        accountId, balance, request.getAmount());
                                                return updateOperationStatus(operationUuid, "FAILED")
                                                        .then(Mono.just(CashResponse.builder()
                                                                .status("ERROR")
                                                                .message("Недостаточно средств")
                                                                .build()));
                                            }
                                            return accountsServiceClient.updateAccountBalance(BalanceUpdateRequestDto.builder()
                                                            .accountId(accountId)
                                                            .amount(request.getAmount())
                                                            .isDeposit(false)
                                                            .build())
                                                    .then(notificationsServiceClient.sendNotification(NotificationRequestDto.builder()
                                                            .userId(request.getUserId())
                                                            .message("Снято " + request.getAmount() + " " + request.getCurrency() + " со счета " + request.getAccountId())
                                                            .build()))
                                                    .then(updateOperationStatus(operationUuid, "COMPLETED"))
                                                    .thenReturn(CashResponse.builder()
                                                            .status("SUCCESS")
                                                            .message("Снятие успешно выполнено")
                                                            .build())
                                                    .doOnSuccess(response ->
                                                            log.info("Операция снятия успешно завершена: {}", operationUuid))
                                                    .onErrorResume(e -> {
                                                        log.error("Ошибка при снятии: {}", e.getMessage(), e);
                                                        return updateOperationStatus(operationUuid, "FAILED")
                                                                .then(Mono.error(e));
                                                    });
                                        });
                            });
                })
                .onErrorResume(e -> {
                    log.error("Ошибка в процессе снятия: {}", e.getMessage(), e);
                    return updateOperationStatus(operationUuid, "FAILED")
                            .then(Mono.just(CashResponse.builder()
                                    .status("ERROR")
                                    .message(e.getMessage())
                                    .build()));
                });
    }

    private Mono<Void> validateRequest(CashRequest request) {
        if (request.getAccountId() == null) {
            return Mono.error(new IllegalArgumentException("Идентификатор счета обязателен"));
        }
        if (request.getUserId() == null) {
            return Mono.error(new IllegalArgumentException("Идентификатор пользователя обязателен"));
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Сумма должна быть положительной"));
        }
        if (request.getCurrency() == null || request.getCurrency().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Валюта обязательна"));
        }

        return Mono.empty();
    }

    private Mono<Void> updateOperationStatus(UUID operationUuid, String status) {
        return cashOperationRepository.findById(operationUuid)
                .flatMap(operation -> {
                    operation.setStatus(status);
                    operation.setCompletedAt(LocalDateTime.now());
                    return cashOperationRepository.save(operation);
                })
                .then();
    }
}