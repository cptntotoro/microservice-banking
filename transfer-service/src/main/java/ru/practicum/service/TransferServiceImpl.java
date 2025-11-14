package ru.practicum.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.util.function.Tuple2;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.client.account.dto.AccountResponseDto;
import ru.practicum.client.account.dto.TransferDto;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.blocker.dto.OperationCheckRequestDto;
import ru.practicum.client.exchange.ExchangeServiceClient;
import ru.practicum.client.exchange.dto.ExchangeRequestDto;
import ru.practicum.client.exchange.dto.ExchangeResponseDto;
import ru.practicum.dao.TransferDao;
import ru.practicum.dto.NotificationRequestDto;
import ru.practicum.dto.OtherTransferRequestDto;
import ru.practicum.dto.OwnTransferRequestDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.TransferMapper;
import ru.practicum.model.OperationStatus;
import ru.practicum.model.TransferResponse;
import ru.practicum.model.TransferType;
import ru.practicum.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    /**
     * Клиент для сервиса аккаунтов
     */
    private final AccountServiceClient accountServiceClient;

    /**
     * Клиент для сервиса блокировки подозрительных операций
     */
    private final BlockerServiceClient blockerServiceClient;

    private final KafkaSender<String, NotificationRequestDto> kafkaSender;

    /**
     * Клиент для сервиса обмена валют
     */
    private final ExchangeServiceClient exchangeServiceClient;

    /**
     * Репозиторий операций перевода средств
     */
    private final TransferRepository transferRepository;

    /**
     * Маппер запросов на перевод
     */
    private final TransferMapper transferMapper;

    @Transactional
    @Override
    public Mono<TransferResponse> transferBetweenOwnAccounts(@Valid OwnTransferRequestDto request) {
        UUID fromAccountId = request.getFromAccountId();
        UUID toAccountId = request.getToAccountId();
        UUID userId = request.getUserId();
        BigDecimal amount = request.getAmount();
        LocalDateTime timestamp = LocalDateTime.now();
        TransferType type = TransferType.OWN_TRANSFER;

        return retrieveAccountsByIds(fromAccountId, toAccountId)
                .flatMap(tuple -> processTransferWithValidation(tuple.getT1(), tuple.getT2(), amount, type, timestamp, userId))
                .doOnSuccess(response -> log.info("Перевод {} со счета {} на счет {} на сумму {} выполнен",
                        type, fromAccountId, toAccountId, amount))
                .doOnError(error -> logError(error, type));
    }

    @Transactional
    @Override
    public Mono<TransferResponse> transferToOtherAccount(OtherTransferRequestDto requestDto) {
        UUID fromAccountId = requestDto.getFromAccountId();
        UUID userId = requestDto.getFromUserId();
        String recipientEmail = requestDto.getRecipientEmail();
        String toCurrency = requestDto.getToCurrency();
        BigDecimal amount = requestDto.getAmount();
        LocalDateTime timestamp = LocalDateTime.now();
        TransferType type = TransferType.EXTERNAL_TRANSFER;

        return retrieveAccountsByEmail(fromAccountId, recipientEmail, toCurrency)
                .flatMap(tuple -> processTransferWithValidation(tuple.getT1(), tuple.getT2(), amount, type, timestamp, userId))
                .doOnSuccess(response -> log.info("Перевод {} со счета {} на email {} на сумму {} выполнен",
                        type, fromAccountId, recipientEmail, amount))
                .doOnError(error -> logError(error, type));
    }

    private Mono<Tuple2<AccountResponseDto, AccountResponseDto>> retrieveAccountsByIds(UUID fromAccountId, UUID toAccountId) {
        return Mono.zip(
                fetchAccountByAccountId(fromAccountId, "Счет отправителя"),
                fetchAccountByAccountId(toAccountId, "Счет получателя")
        );
    }

    private Mono<Tuple2<AccountResponseDto, AccountResponseDto>> retrieveAccountsByEmail(UUID fromAccountId, String recipientEmail, String toCurrency) {
        return Mono.zip(
                fetchAccountByAccountId(fromAccountId, "Счет отправителя"),
                accountServiceClient.getAccountWithUserByEmailAndCurrency(recipientEmail, toCurrency)
                        .onErrorResume(NotFoundException.class,
                                e -> Mono.error(new ValidationException("Счет получателя не найден")))
                        .onErrorResume(ServiceUnavailableException.class,
                                e -> Mono.error(new ServiceUnavailableException("account-service",
                                        "Не удалось получить счет получателя")))
        );
    }

    private Mono<AccountResponseDto> fetchAccountByAccountId(UUID accountId, String accountType) {
        return accountServiceClient.getAccountWithUserByAccountId(accountId)
                .onErrorResume(NotFoundException.class,
                        e -> Mono.error(new ValidationException(accountType + " не найден")))
                .onErrorResume(ServiceUnavailableException.class,
                        e -> Mono.error(new ServiceUnavailableException("account-service",
                                "Не удалось получить " + accountType.toLowerCase())));
    }

    private Mono<TransferResponse> processTransferWithValidation(AccountResponseDto fromAccount, AccountResponseDto toAccount,
                                                                 BigDecimal amount, TransferType type, LocalDateTime timestamp, UUID userId) {
        return validateInitialTransfer(fromAccount, toAccount, amount, type, userId, timestamp)
                .then(processTransfer(fromAccount, toAccount, amount, type, userId, timestamp));
    }

    private Mono<Void> validateInitialTransfer(AccountResponseDto fromAccount, AccountResponseDto toAccount, BigDecimal amount, TransferType type, UUID userId, LocalDateTime timestamp) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            return saveFailedTransfer(fromAccount.getId(), toAccount.getId(), amount,
                    fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null,
                    timestamp, type, "Недостаточно средств на счете отправителя")
                    .then(Mono.error(new ValidationException("Недостаточно средств на счете отправителя")));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return saveFailedTransfer(fromAccount.getId(), toAccount.getId(), amount, fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null,
                    timestamp, type, "Сумма перевода должна быть больше нуля")
                    .then(Mono.error(new ValidationException("Сумма перевода должна быть больше нуля")));
        }
        if (fromAccount.getId().equals(toAccount.getId())) {
            String errorMessage = "Счета отправителя и получателя не могут быть одинаковыми";
            return saveFailedTransfer(fromAccount.getId(), toAccount.getId(), amount, fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null,
                    timestamp, type, errorMessage)
                    .then(Mono.error(new ValidationException(errorMessage)));
        }
        if (type == TransferType.OWN_TRANSFER &&
                (!fromAccount.getUserId().equals(toAccount.getUserId()) ||
                        !fromAccount.getUserId().equals(userId) ||
                        !toAccount.getUserId().equals(userId))) {
            String errorMessage = "Пользователю не принадлежит ни один из счетов";
            return saveFailedTransfer(fromAccount.getId(), toAccount.getId(), amount, fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null,
                    timestamp, type, errorMessage)
                    .then(Mono.error(new ValidationException(errorMessage)));
        }
        if (type == TransferType.EXTERNAL_TRANSFER &&
                (fromAccount.getUserId().equals(toAccount.getUserId()) ||
                        !fromAccount.getUserId().equals(userId) ||
                        toAccount.getUserId().equals(userId))) {
            String errorMessage = "Пользователю принадлежат оба счета или пользователю не принадлежит исходящий или целевой счет ";
            return saveFailedTransfer(fromAccount.getId(), toAccount.getId(), amount, fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), null,
                    timestamp, type, errorMessage)
                    .then(Mono.error(new ValidationException(errorMessage)));
        }
        return Mono.empty();
    }

    private Mono<TransferResponse> processTransfer(AccountResponseDto fromAccount, AccountResponseDto toAccount, BigDecimal amount,
                                                   TransferType type, UUID userId, LocalDateTime timestamp) {
        String fromCode = fromAccount.getCurrencyCode();
        String toCode = toAccount.getCurrencyCode();
        OperationCheckRequestDto checkRequest = buildOperationCheckRequest(fromAccount.getId(), amount, fromCode, userId, timestamp);

        return checkOperationAndConvert(fromAccount.getId(), toAccount.getId(), amount, fromCode, toCode, type, timestamp, checkRequest);
    }

    private OperationCheckRequestDto buildOperationCheckRequest(UUID accountId, BigDecimal amount, String currency, UUID userId, LocalDateTime timestamp) {
        return OperationCheckRequestDto.builder()
                .operationId(UUID.randomUUID())
                .operationType("TRANSFER")
                .userId(userId)
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .timestamp(timestamp)
                .build();
    }

    private Mono<TransferResponse> checkOperationAndConvert(UUID fromAccountId, UUID toAccountId, BigDecimal amount,
                                                            String fromCode, String toCode, TransferType type,
                                                            LocalDateTime timestamp, OperationCheckRequestDto checkRequest) {
        return blockerServiceClient.checkOperation(checkRequest)
                .onErrorResume(ServiceUnavailableException.class, e ->
                        saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null,
                                timestamp, type, "Не удалось проверить безопасность операции")
                                .then(Mono.error(new ServiceUnavailableException("blocker-service",
                                        "Не удалось проверить безопасность операции"))))
                .flatMap(checkResponse -> {
                    if (checkResponse.isBlocked()) {
                        return saveFailedTransfer(fromAccountId, toAccountId, amount, fromCode, toCode, null,
                                timestamp, type, "Операция заблокирована как подозрительная: " + checkResponse.getDescription())
                                .then(Mono.error(new ValidationException(
                                        "Операция заблокирована как подозрительная: " + checkResponse.getDescription())));
                    }
                    return executeTransferAndNotify(fromAccountId, toAccountId, amount, fromCode, toCode, type, timestamp);
                });
    }

    private Mono<TransferResponse> executeTransferAndNotify(UUID fromAccountId, UUID toAccountId, BigDecimal amount,
                                                            String fromCode, String toCode, TransferType type,
                                                            LocalDateTime timestamp) {
        return convertCurrency(fromCode, toCode, amount)
                .flatMap(convertResponse -> {
                    BigDecimal convertedAmount = convertResponse.getConvertedAmount();
                    return executeTransfer(fromAccountId, toAccountId, amount, convertedAmount, type)
                            .thenReturn(convertedAmount);
                })
                .flatMap(converted -> sendNotification(fromAccountId, amount, fromCode, converted, toCode, type)
                        .thenReturn(converted))
                .flatMap(converted -> buildAndSaveSuccessResponse(fromAccountId, toAccountId, amount, converted, fromCode, toCode, timestamp, type));
    }

    private Mono<ExchangeResponseDto> convertCurrency(String fromCode, String toCode, BigDecimal amount) {
        return exchangeServiceClient.convertCurrency(ExchangeRequestDto.builder()
                        .fromCurrency(fromCode)
                        .toCurrency(toCode)
                        .amount(amount)
                        .build())
                .onErrorResume(ValidationException.class, e ->
                        saveFailedTransfer(null, null, amount, fromCode, toCode, null,
                                LocalDateTime.now(), null, "Ошибка конвертации: " + e.getMessage())
                                .then(Mono.error(new ValidationException("Ошибка конвертации: " + e.getMessage()))))
                .onErrorResume(ServiceUnavailableException.class, e ->
                        saveFailedTransfer(null, null, amount, fromCode, toCode, null,
                                LocalDateTime.now(), null, "Не удалось выполнить конвертацию")
                                .then(Mono.error(new ServiceUnavailableException("exchange-service",
                                        "Не удалось выполнить конвертацию"))));
    }

    private Mono<Void> executeTransfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, BigDecimal convertedAmount, TransferType type) {
        TransferDto transferDto = TransferDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();
        return accountServiceClient.transfer(transferDto)
                .onErrorResume(e ->
                        saveFailedTransfer(fromAccountId, toAccountId, amount, null, null, convertedAmount,
                                LocalDateTime.now(), type, "Ошибка при переводе: " + e.getMessage())
                                .then(Mono.error(e)));
    }

    private Mono<TransferResponse> buildAndSaveSuccessResponse(UUID fromAccountId, UUID toAccountId, BigDecimal amount,
                                                               BigDecimal converted, String fromCode, String toCode,
                                                               LocalDateTime timestamp, TransferType type) {
        TransferResponse response = TransferResponse.builder()
                .status(OperationStatus.SUCCESS)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(converted)
                .build();
        return saveSuccessfulTransfer(response, fromCode, toCode, timestamp, type)
                .then(Mono.just(response));
    }

    private Mono<Void> sendNotification(UUID fromAccountId, BigDecimal amount, String fromCode,
                                        BigDecimal converted, String toCode, TransferType type) {
        String message = String.format("Перевод %s на сумму %s %s выполнен. Конвертировано в %s %s",
                type == TransferType.OWN_TRANSFER ? "между своими счетами" : "на другой счет",
                amount, fromCode, converted, toCode);
        SenderRecord<String, NotificationRequestDto, String> record = SenderRecord.create(
                new ProducerRecord<>("notification", "message", NotificationRequestDto.builder()
                        .userId(fromAccountId)
                        .message(message)
                        .build()),
                UUID.randomUUID().toString()
        );
        return kafkaSender.send(Mono.just(record))
                .next()
                .doOnSuccess(result -> {
                    if (result.exception() != null) {
                        log.warn("Kafka send failed: {}", result.exception().getMessage());
                    } else {
                        log.info("Notification sent, offset: {}", result.recordMetadata().offset());
                    }
                }).retry(3).onErrorResume(e -> {
                    log.warn("Failed to send notification after retries: {}", e.getMessage());
                    return Mono.empty(); // не прерываем транзакцию
                })
                .then();
    }

    private void logError(Throwable error, TransferType type) {
        if (error instanceof ValidationException) {
            log.warn("Ошибка валидации при переводе {}: {}", type, error.getMessage());
        } else {
            log.error("Ошибка при переводе {}: {}", type, error.getMessage(), error);
        }
    }

    private Mono<Void> saveSuccessfulTransfer(TransferResponse response, String fromCurrency,
                                              String toCurrency, LocalDateTime timestamp,
                                              TransferType type) {
        TransferDao dao = transferMapper.transferResponseToTransferDao(response, fromCurrency,
                toCurrency, timestamp, type, null);
        return Mono.fromCallable(() -> transferRepository.save(dao)).then();
    }

    private Mono<Void> saveFailedTransfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount,
                                          String fromCurrency, String toCurrency,
                                          BigDecimal convertedAmount, LocalDateTime timestamp,
                                          TransferType type, String errorDescription) {
        TransferResponse failedResponse = TransferResponse.builder()
                .status(OperationStatus.FAILED)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();
        TransferDao dao = transferMapper.transferResponseToTransferDao(failedResponse, fromCurrency,
                toCurrency, timestamp, type, errorDescription);
        return Mono.fromCallable(() -> transferRepository.save(dao)).then();
    }
}