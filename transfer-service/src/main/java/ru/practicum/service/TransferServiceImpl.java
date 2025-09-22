package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.AccountResponseDto;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.blocker.OperationCheckRequestDto;
import ru.practicum.client.exchange.ExchangeServiceClient;
import ru.practicum.client.notification.NotificationsServiceClient;
import ru.practicum.dao.TransferDao;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.TransferMapper;
import ru.practicum.model.OperationStatus;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;
import ru.practicum.model.TransferType;
import ru.practicum.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Реализация сервиса для обработки переводов между счетами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    /**
     * Клиент для обращений к сервису аккаунтов
     */
    private final AccountServiceClient accountServiceClient;

    /**
     * Клиент для обращений к сервису блокировки операций
     */
    private final BlockerServiceClient blockerServiceClient;

    /**
     * Клиент для обращений к сервису оповещений
     */
    private final NotificationsServiceClient notificationsServiceClient;

    /**
     * Клиент для обращений к сервису конвертации валют
     */
    private final ExchangeServiceClient exchangeServiceClient;

    /**
     * Репозиторий для сохранения операций перевода
     */
    private final TransferRepository transferRepository;

    /**
     * Маппер для конвертации моделей
     */
    private final TransferMapper transferMapper;

    @Transactional
    @Override
    public Mono<TransferResponse> transferBetweenOwnAccounts(TransferRequest request) {
        UUID fromAccountId = request.getFromAccountId();
        UUID toAccountId = request.getToAccountId();
        BigDecimal amount = request.getAmount();
        LocalDateTime timestamp = LocalDateTime.now();
        TransferType type = TransferType.OWN_TRANSFER;

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return saveFailedTransfer(fromAccountId, toAccountId, amount, null, null, null, timestamp, type, "Сумма перевода должна быть больше нуля")
                    .then(Mono.error(new ValidationException("Сумма перевода должна быть больше нуля")));
        }
        if (fromAccountId.equals(toAccountId)) {
            return saveFailedTransfer(fromAccountId, toAccountId, amount, null, null, null, timestamp, type, "Счета отправителя и получателя не могут быть одинаковыми")
                    .then(Mono.error(new ValidationException("Счета отправителя и получателя не могут быть одинаковыми")));
        }

        return Mono.zip(
                        accountServiceClient.getAccountById(fromAccountId)
                                .onErrorResume(NotFoundException.class, e -> Mono.error(new ValidationException("Счет отправителя не найден")))
                                .onErrorResume(ServiceUnavailableException.class, e -> Mono.error(new ServiceUnavailableException("account-service", "Не удалось получить счет отправителя"))),
                        accountServiceClient.getAccountById(toAccountId)
                                .onErrorResume(NotFoundException.class, e -> Mono.error(new ValidationException("Счет получателя не найден")))
                                .onErrorResume(ServiceUnavailableException.class, e -> Mono.error(new ServiceUnavailableException("account-service", "Не удалось получить счет получателя")))
                ).flatMap(tuple -> {
                    AccountResponseDto fromAccount = tuple.getT1();
                    AccountResponseDto toAccount = tuple.getT2();

                    if (!fromAccount.getUserId().equals(toAccount.getUserId())) {
                        return saveFailedTransfer(fromAccountId, toAccountId, amount, fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null, timestamp, type, "Счета должны принадлежать одному пользователю")
                                .then(Mono.error(new ValidationException("Счета должны принадлежать одному пользователю")));
                    }

                    if (fromAccount.getBalance().compareTo(amount) < 0) {
                        return saveFailedTransfer(fromAccountId, toAccountId, amount, fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null, timestamp, type, "Недостаточно средств на счете отправителя")
                                .then(Mono.error(new ValidationException("Недостаточно средств на счете отправителя")));
                    }

                    String fromCode = fromAccount.getCurrencyCode();
                    String toCode = toAccount.getCurrencyCode();
                    UUID userId = fromAccount.getUserId();

                    OperationCheckRequestDto checkRequest = OperationCheckRequestDto.builder()
                            .operationId(UUID.randomUUID())
                            .operationType("TRANSFER")
                            .userId(userId)
                            .accountId(fromAccountId)
                            .amount(amount)
                            .currency(fromCode)
                            .timestamp(timestamp)
                            .build();

                    return blockerServiceClient.checkOperation(checkRequest)
                            .onErrorResume(ServiceUnavailableException.class, e ->
                                    saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null, timestamp, type, "Не удалось проверить безопасность операции")
                                            .then(Mono.error(new ServiceUnavailableException("blocker-service", "Не удалось проверить безопасность операции"))))
                            .flatMap(checkResponse -> {
                                if (checkResponse.isBlocked()) {
                                    return saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null, timestamp, type, "Операция заблокирована как подозрительная: " + checkResponse.getDescription())
                                            .then(Mono.error(new ValidationException("Операция заблокирована как подозрительная: " + checkResponse.getDescription())));
                                }

                                return exchangeServiceClient.convertCurrency(fromCode, toCode, amount)
                                        .onErrorResume(ValidationException.class, e ->
                                                saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null, timestamp, type, "Ошибка конвертации: " + e.getMessage())
                                                        .then(Mono.error(new ValidationException("Ошибка конвертации: " + e.getMessage()))))
                                        .onErrorResume(ServiceUnavailableException.class, e ->
                                                saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null, timestamp, type, "Не удалось выполнить конвертацию")
                                                        .then(Mono.error(new ServiceUnavailableException("exchange-service", "Не удалось выполнить конвертацию"))))
                                        .flatMap(convertResponse -> {
                                            BigDecimal converted = convertResponse.getConvertedAmount();
                                            return accountServiceClient.transferBetweenOwnAccounts(fromAccountId, toAccountId, amount, converted)
                                                    .onErrorResume(e ->
                                                            saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, converted, timestamp, type, "Ошибка при переводе: " + e.getMessage())
                                                                    .then(Mono.error(e)))
                                                    .thenReturn(converted);
                                        })
                                        .flatMap(converted ->
                                                notificationsServiceClient.sendNotification(
                                                        fromAccountId,
                                                        String.format("Перевод между своими счетами на сумму %s %s выполнен. Конвертировано в %s %s",
                                                                amount, fromCode, converted, toCode)
                                                ).onErrorResume(e -> {
                                                    log.warn("Не удалось отправить уведомление: {}", e.getMessage());
                                                    return Mono.empty();
                                                }).thenReturn(converted)
                                        )
                                        .map(converted -> TransferResponse.builder()
                                                .status(OperationStatus.SUCCESS)
                                                .fromAccountId(fromAccountId)
                                                .toAccountId(toAccountId)
                                                .amount(amount)
                                                .convertedAmount(converted)
                                                .build())
                                        .flatMap(response ->
                                                saveSuccessfulTransfer(response, fromCode, toCode, timestamp, type)
                                                        .then(Mono.just(response))
                                        );
                            });
                }).doOnSuccess(response -> log.info("Перевод между своими счетами {} и {} на сумму {} выполнен", fromAccountId, toAccountId, amount))
                .doOnError(error -> {
                    if (error instanceof ValidationException) {
                        log.warn("Ошибка валидации при переводе: {}", error.getMessage());
                    } else {
                        log.error("Ошибка при переводе между своими счетами: {}", error.getMessage(), error);
                    }
                });
    }

    @Transactional
    @Override
    public Mono<TransferResponse> transferToOtherAccount(TransferRequest request) {
        UUID fromAccountId = request.getFromAccountId();
        String toAccountNumber = request.getToAccountNumber();
        BigDecimal amount = request.getAmount();
        LocalDateTime timestamp = LocalDateTime.now();
        TransferType type = TransferType.EXTERNAL_TRANSFER;

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return saveFailedTransfer(fromAccountId, null, amount, null, null, null, timestamp, type, "Сумма перевода должна быть больше нуля")
                    .then(Mono.error(new ValidationException("Сумма перевода должна быть больше нуля")));
        }

        return Mono.zip(
                        accountServiceClient.getAccountById(fromAccountId)
                                .onErrorResume(NotFoundException.class, e -> Mono.error(new ValidationException("Счет отправителя не найден")))
                                .onErrorResume(ServiceUnavailableException.class, e -> Mono.error(new ServiceUnavailableException("account-service", "Не удалось получить счет отправителя"))),
                        accountServiceClient.getAccountByNumber(toAccountNumber)
                                .onErrorResume(NotFoundException.class, e -> Mono.error(new ValidationException("Счет получателя не найден")))
                                .onErrorResume(ServiceUnavailableException.class, e -> Mono.error(new ServiceUnavailableException("account-service", "Не удалось получить счет получателя")))
                ).flatMap(tuple -> {
                    AccountResponseDto fromAccount = tuple.getT1();
                    AccountResponseDto toAccount = tuple.getT2();
                    UUID toAccountId = toAccount.getAccountId();

                    if (fromAccountId.equals(toAccountId)) {
                        return saveFailedTransfer(fromAccountId, toAccountId, amount, fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null, timestamp, type, "Перевод на собственный счет должен выполняться через transferBetweenOwnAccounts")
                                .then(Mono.error(new ValidationException("Перевод на собственный счет должен выполняться через transferBetweenOwnAccounts")));
                    }

                    if (fromAccount.getBalance().compareTo(amount) < 0) {
                        return saveFailedTransfer(fromAccountId, toAccountId, amount, fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null, timestamp, type, "Недостаточно средств на счете отправителя")
                                .then(Mono.error(new ValidationException("Недостаточно средств на счете отправителя")));
                    }

                    String fromCode = fromAccount.getCurrencyCode();
                    String toCode = toAccount.getCurrencyCode();
                    UUID userId = fromAccount.getUserId();

                    OperationCheckRequestDto checkRequest = OperationCheckRequestDto.builder()
                            .operationId(UUID.randomUUID())
                            .operationType("TRANSFER")
                            .userId(userId)
                            .accountId(fromAccountId)
                            .amount(amount)
                            .currency(fromCode)
                            .timestamp(timestamp)
                            .build();

                    return blockerServiceClient.checkOperation(checkRequest)
                            .onErrorResume(ServiceUnavailableException.class, e ->
                                    saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null, timestamp, type, "Не удалось проверить безопасность операции")
                                            .then(Mono.error(new ServiceUnavailableException("blocker-service", "Не удалось проверить безопасность операции"))))
                            .flatMap(checkResponse -> {
                                if (checkResponse.isBlocked()) {
                                    return saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null, timestamp, type, "Операция заблокирована как подозрительная: " + checkResponse.getDescription())
                                            .then(Mono.error(new ValidationException("Операция заблокирована как подозрительная: " + checkResponse.getDescription())));
                                }

                                return exchangeServiceClient.convertCurrency(fromCode, toCode, amount)
                                        .onErrorResume(ValidationException.class, e ->
                                                saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null, timestamp, type, "Ошибка конвертации: " + e.getMessage())
                                                        .then(Mono.error(new ValidationException("Ошибка конвертации: " + e.getMessage()))))
                                        .onErrorResume(ServiceUnavailableException.class, e ->
                                                saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null, timestamp, type, "Не удалось выполнить конвертацию")
                                                        .then(Mono.error(new ServiceUnavailableException("exchange-service", "Не удалось выполнить конвертацию"))))
                                        .flatMap(convertResponse -> {
                                            BigDecimal converted = convertResponse.getConvertedAmount();
                                            return accountServiceClient.transferToOtherAccount(fromAccountId, toAccountNumber, amount, converted)
                                                    .onErrorResume(e ->
                                                            saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, converted, timestamp, type, "Ошибка при переводе: " + e.getMessage())
                                                                    .then(Mono.error(e)))
                                                    .thenReturn(converted);
                                        })
                                        .flatMap(converted ->
                                                Mono.when(
                                                        notificationsServiceClient.sendNotification(
                                                                fromAccountId,
                                                                String.format("Перевод на другой счет на сумму %s %s выполнен. Конвертировано в %s %s",
                                                                        amount, fromCode, converted, toCode)
                                                        ),
                                                        notificationsServiceClient.sendNotification(
                                                                toAccountId,
                                                                String.format("Получен перевод на сумму %s %s от счета %s",
                                                                        converted, toCode, fromAccountId)
                                                        )
                                                ).onErrorResume(e -> {
                                                    log.warn("Не удалось отправить уведомление: {}", e.getMessage());
                                                    return Mono.empty();
                                                }).thenReturn(converted)
                                        )
                                        .map(converted -> TransferResponse.builder()
                                                .status(OperationStatus.SUCCESS)
                                                .fromAccountId(fromAccountId)
                                                .toAccountId(toAccountId)
                                                .amount(amount)
                                                .convertedAmount(converted)
                                                .build())
                                        .flatMap(response ->
                                                saveSuccessfulTransfer(response, fromCode, toCode, timestamp, type)
                                                        .then(Mono.just(response))
                                        );
                            });
                }).doOnSuccess(response -> log.info("Перевод со счета {} на счет {} на сумму {} выполнен", fromAccountId, toAccountNumber, amount))
                .doOnError(error -> {
                    if (error instanceof ValidationException) {
                        log.warn("Ошибка валидации при переводе: {}", error.getMessage());
                    } else {
                        log.error("Ошибка при переводе на другой счет: {}", error.getMessage(), error);
                    }
                });
    }

    private Mono<Void> saveSuccessfulTransfer(TransferResponse response, String fromCurrency, String toCurrency, LocalDateTime timestamp, TransferType type) {
        TransferDao dao = transferMapper.toDao(response, fromCurrency, toCurrency, timestamp, type, null);
        return Mono.fromCallable(() -> transferRepository.save(dao)).then();
    }

    private Mono<Void> saveFailedTransfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String fromCurrency, String toCurrency, BigDecimal convertedAmount, LocalDateTime timestamp, TransferType type, String errorDescription) {
        TransferResponse failedResponse = TransferResponse.builder()
                .status(OperationStatus.FAILED)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();
        TransferDao dao = transferMapper.toDao(failedResponse, fromCurrency, toCurrency, timestamp, type, errorDescription);
        return Mono.fromCallable(() -> transferRepository.save(dao)).then();
    }
}
