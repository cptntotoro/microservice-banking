package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.AccountResponseDto;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.exchange.ExchangeServiceClient;
import ru.practicum.client.notification.NotificationsServiceClient;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;

import java.math.BigDecimal;
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

    @Transactional
    @Override
    public Mono<TransferResponse> transferBetweenOwnAccounts(TransferRequest request) {
        UUID fromAccountId = request.getFromAccountId();
        UUID toAccountId = request.getToAccountId();
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ValidationException("Сумма перевода должна быть больше нуля"));
        }
        if (fromAccountId.equals(toAccountId)) {
            return Mono.error(new ValidationException("Счета отправителя и получателя не могут быть одинаковыми"));
        }

        return Mono.zip(
                accountServiceClient.getAccountById(fromAccountId)
                        .onErrorResume(NotFoundException.class, e ->
                                Mono.error(new ValidationException("Счет отправителя не найден")))
                        .onErrorResume(ServiceUnavailableException.class, e ->
                                Mono.error(new ServiceUnavailableException("account-service", "Не удалось получить счет отправителя"))),

                accountServiceClient.getAccountById(toAccountId)
                        .onErrorResume(NotFoundException.class, e ->
                                Mono.error(new ValidationException("Счет получателя не найден")))
                        .onErrorResume(ServiceUnavailableException.class, e ->
                                Mono.error(new ServiceUnavailableException("account-service", "Не удалось получить счет получателя")))
        ).flatMap(tuple -> {
            AccountResponseDto fromAccount = tuple.getT1();
            AccountResponseDto toAccount = tuple.getT2();

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                return Mono.error(new ValidationException("Недостаточно средств на счете отправителя"));
            }

            String fromCode = fromAccount.getCurrencyCode();
            String toCode = toAccount.getCurrencyCode();

            return exchangeServiceClient.convertCurrency(fromCode, toCode, amount)
                    .onErrorResume(ValidationException.class, e ->
                            Mono.error(new ValidationException("Ошибка конвертации: " + e.getMessage())))
                    .onErrorResume(ServiceUnavailableException.class, e ->
                            Mono.error(new ServiceUnavailableException("exchange-service", "Не удалось выполнить конвертацию")))
                    .flatMap(convertResponse ->
                            blockerServiceClient.checkSuspicious(fromAccountId, toAccountId, amount, true)
                                    .onErrorResume(ServiceUnavailableException.class, e ->
                                            Mono.error(new ServiceUnavailableException("blocker-service", "Не удалось проверить безопасность операции")))
                                    .flatMap(isSuspicious -> {
                                        if (isSuspicious) {
                                            return Mono.error(new ValidationException("Операция заблокирована как подозрительная"));
                                        }
                                        return accountServiceClient.transferBetweenOwnAccounts(
                                                fromAccountId,
                                                toAccountId,
                                                amount
                                        ).then(Mono.just(convertResponse));
                                    })
                    )
                    .flatMap(convertResponse ->
                            notificationsServiceClient.sendNotification(
                                    fromAccountId,
                                    String.format("Перевод между своими счетами на сумму %s %s выполнен. Конвертировано в %s %s",
                                            amount, fromCode, convertResponse.getConvertedAmount(), toCode)
                            ).onErrorResume(e -> {
                                log.warn("Не удалось отправить уведомление: {}", e.getMessage());
                                return Mono.empty(); // Продолжаем выполнение даже при ошибке уведомления
                            }).then(Mono.just(convertResponse))
                    )
                    .map(convertResponse -> TransferResponse.builder()
                            .status("SUCCESS")
                            .fromAccountId(fromAccountId)
                            .toAccountId(toAccountId)
                            .amount(amount)
                            .convertedAmount(convertResponse.getConvertedAmount())
                            .build())
                    .doOnSuccess(response -> log.info("Перевод между своими счетами {} и {} на сумму {} выполнен", fromAccountId, toAccountId, amount))
                    .doOnError(error -> {
                        if (error instanceof ValidationException) {
                            log.warn("Ошибка валидации при переводе: {}", error.getMessage());
                        } else {
                            log.error("Ошибка при переводе между своими счетами: {}", error.getMessage(), error);
                        }
                    });
        });
    }

    @Transactional
    @Override
    public Mono<TransferResponse> transferToOtherAccount(TransferRequest request) {
        UUID fromAccountId = request.getFromAccountId();
        String toAccountNumber = request.getToAccountNumber();
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ValidationException("Сумма перевода должна быть больше нуля"));
        }

        return Mono.zip(
                accountServiceClient.getAccountById(fromAccountId),
                accountServiceClient.getAccountByNumber(toAccountNumber)
        ).flatMap(tuple -> {
            AccountResponseDto fromAccount = tuple.getT1();
            AccountResponseDto toAccount = tuple.getT2();
            UUID toAccountId = toAccount.getId();

            if (fromAccountId.equals(toAccountId)) {
                return Mono.error(new ValidationException("Перевод на собственный счет должен выполняться через transferBetweenOwnAccounts"));
            }

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                return Mono.error(new ValidationException("Недостаточно средств на счете отправителя"));
            }

            String fromCode = fromAccount.getCurrencyCode();
            String toCode = toAccount.getCurrencyCode();

            // Используем конвертацию через exchange-service
            return exchangeServiceClient.convertCurrency(fromCode, toCode, amount)
                    .flatMap(convertResponse ->
                            blockerServiceClient.checkSuspicious(fromAccountId, toAccountId, amount, false)
                                    .flatMap(isSuspicious -> {
                                        if (isSuspicious) {
                                            return Mono.error(new ValidationException("Операция заблокирована как подозрительная"));
                                        }
                                        return accountServiceClient.transferToOtherAccount(
                                                fromAccountId,
                                                toAccountNumber,
                                                amount
                                        ).then(Mono.just(convertResponse));
                                    })
                    )
                    .flatMap(convertResponse ->
                            Mono.when(
                                    notificationsServiceClient.sendNotification(
                                            fromAccountId,
                                            String.format("Перевод на другой счет на сумму %s %s выполнен. Конвертировано в %s %s",
                                                    amount, fromCode, convertResponse.getConvertedAmount(), toCode)
                                    ),
                                    notificationsServiceClient.sendNotification(
                                            toAccountId,
                                            String.format("Получен перевод на сумму %s %s от счета %s",
                                                    convertResponse.getConvertedAmount(), toCode, fromAccountId)
                                    )
                            ).then(Mono.just(convertResponse))
                    )
                    .map(convertResponse -> TransferResponse.builder()
                            .status("SUCCESS")
                            .fromAccountId(fromAccountId)
                            .toAccountId(toAccountId)
                            .amount(amount)
                            .convertedAmount(convertResponse.getConvertedAmount())
                            .build())
                    .doOnSuccess(response -> log.info("Перевод со счета {} на счет {} на сумму {} выполнен", fromAccountId, toAccountNumber, amount))
                    .doOnError(error -> log.error("Ошибка при переводе на другой счет: {}", error.getMessage(), error));
        });
    }
}
