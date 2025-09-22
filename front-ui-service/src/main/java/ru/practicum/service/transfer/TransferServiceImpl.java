package ru.practicum.service.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.account.AccountResponseClientDto;
import ru.practicum.client.account.account.AccountServiceClient;
import ru.practicum.client.transfer.TransferServiceClient;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.exception.transfer.TransferException;
import ru.practicum.model.transfer.OtherTransfer;
import ru.practicum.model.transfer.OwnTransfer;
import ru.practicum.model.transfer.TransferErrorCode;
import ru.practicum.model.transfer.TransferResult;
import ru.practicum.model.transfer.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final AccountServiceClient accountServiceClient;
    private final TransferServiceClient transferServiceClient;

    @Override
    public Mono<TransferResult> performOwnTransfer(OwnTransfer model) {
        log.info("Processing own transfer for user {}: {} -> {}",
                model.getUserId(), model.getFromAccountId(), model.getToAccountId());

        return Mono.zip(
                        accountServiceClient.getAccount(model.getFromAccountId()),
                        accountServiceClient.getAccount(model.getToAccountId())
                )
                .flatMap(accounts -> {
                    AccountResponseClientDto fromAccount = accounts.getT1();
                    AccountResponseClientDto toAccount = accounts.getT2();

                    // Валидация принадлежности счетов
                    if (!fromAccount.getUserId().equals(model.getUserId()) ||
                            !toAccount.getUserId().equals(model.getUserId())) {
                        return Mono.error(new TransferException(
                                "Счета должны принадлежать текущему пользователю",
                                TransferErrorCode.ACCOUNT_OWNERSHIP_VIOLATION));
                    }

                    // Валидация что счета разные
                    if (fromAccount.getId().equals(toAccount.getId())) {
                        return Mono.error(new TransferException(
                                "Нельзя переводить деньги на тот же счет",
                                TransferErrorCode.SAME_ACCOUNT_TRANSFER));
                    }

                    // Валидация баланса
                    if (fromAccount.getBalance().compareTo(model.getAmount()) < 0) {
                        return Mono.error(new TransferException(
                                "Недостаточно средств на счете",
                                TransferErrorCode.INSUFFICIENT_BALANCE));
                    }

                    if (model.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new TransferException(
                                "Сумма перевода должна быть больше нуля",
                                TransferErrorCode.INVALID_AMOUNT));
                    }

                    // Выполнение перевода
                    OwnTransferRequestDto dto = mapToOwnTransferDto(model);
                    return transferServiceClient.performOwnTransfer(dto, model.getUserId())
                            .then(Mono.just(createSuccessResult(
                                    model.getFromAccountId(),
                                    model.getToAccountId(),
                                    model.getAmount(),
                                    "Перевод между своими счетами выполнен успешно")))
                            .doOnSuccess(result -> log.info("Own transfer completed successfully for user {}",
                                    model.getUserId()));
                })
                .onErrorResume(TransferException.class, this::createFailedResult)
                .onErrorResume(ServiceUnavailableException.class, e ->
                        createServiceUnavailableResult(model, e, "Ошибка при обращении к внешним сервисам"));
    }

    @Override
    public Mono<TransferResult> performOtherTransfer(OtherTransfer model) {
        log.info("Processing other transfer for user {} to email {}: amount {}",
                model.getUserId(), model.getRecipientEmail(), model.getAmount());

        return accountServiceClient.getAccount(model.getFromAccountId())
                .flatMap(fromAccount -> {
                    // Валидация принадлежности счета
                    if (!fromAccount.getUserId().equals(model.getUserId())) {
                        return Mono.error(new TransferException(
                                "Счет отправителя не принадлежит вам",
                                TransferErrorCode.ACCOUNT_OWNERSHIP_VIOLATION));
                    }

                    // Валидация баланса
                    if (fromAccount.getBalance().compareTo(model.getAmount()) < 0) {
                        return Mono.error(new TransferException(
                                "Недостаточно средств на счете",
                                TransferErrorCode.INSUFFICIENT_BALANCE));
                    }

                    if (model.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new TransferException(
                                "Сумма перевода должна быть больше нуля",
                                TransferErrorCode.INVALID_AMOUNT));
                    }

                    if (model.getRecipientEmail() == null || model.getRecipientEmail().trim().isEmpty()) {
                        return Mono.error(new TransferException(
                                "Email получателя обязателен",
                                TransferErrorCode.RECIPIENT_NOT_FOUND));
                    }

                    // Выполнение перевода
                    OtherTransferRequestDto dto = mapToOtherTransferDto(model);
                    return transferServiceClient.performOtherTransfer(dto, model.getUserId())
                            .then(Mono.just(createSuccessResult(
                                    model.getFromAccountId(),
                                    model.getAmount(),
                                    model.getToCurrency(),
                                    model.getRecipientEmail(),
                                    "Перевод другому пользователю выполнен успешно")))
                            .doOnSuccess(result -> log.info("Other transfer completed successfully for user {} to {}",
                                    model.getUserId(), model.getRecipientEmail()));
                })
                .onErrorResume(TransferException.class, this::createFailedResult)
                .onErrorResume(ServiceUnavailableException.class, e ->
                        createServiceUnavailableResult(model, e, "Ошибка при обращении к внешним сервисам"));
    }

    /**
     * Создание результата ошибки перевода
     */
    private Mono<TransferResult> createFailedResult(TransferException e) {
        log.error("Transfer failed with code {}: {}", e.getErrorCode(), e.getMessage());

        // Для own transfer
        if (e.getMessage().contains("своими счетами") || e.getErrorCode() == TransferErrorCode.SAME_ACCOUNT_TRANSFER) {
            return Mono.just(createFailedResult(null, null, null, e.getMessage(), e.getErrorCode()));
        }

        // Для other transfer
        return Mono.just(createFailedResult(null, null, null, e.getMessage(), e.getErrorCode()));
    }

    /**
     * Создание результата недоступности сервиса для own transfer
     */
    private Mono<TransferResult> createServiceUnavailableResult(OwnTransfer model, ServiceUnavailableException e, String baseMessage) {
        String detailedMessage = String.format("%s (%s: %s)", baseMessage, e.getServiceId(), e.getServiceIssue());
        log.error("Service unavailable during own transfer for user {}: {}", model.getUserId(), detailedMessage);

        return Mono.just(createFailedResult(
                model.getFromAccountId(),
                model.getToAccountId(),
                model.getAmount(),
                detailedMessage,
                TransferErrorCode.TRANSFER_SERVICE_UNAVAILABLE));
    }

    /**
     * Создание результата недоступности сервиса для other transfer
     */
    private Mono<TransferResult> createServiceUnavailableResult(OtherTransfer model, ServiceUnavailableException e, String baseMessage) {
        String detailedMessage = String.format("%s (%s: %s)", baseMessage, e.getServiceId(), e.getServiceIssue());
        log.error("Service unavailable during other transfer for user {}: {}", model.getUserId(), detailedMessage);

        return Mono.just(createFailedResult(
                model.getFromAccountId(),
                null,
                model.getAmount(),
                detailedMessage,
                TransferErrorCode.TRANSFER_SERVICE_UNAVAILABLE));
    }

    // Маппинг модели в DTO для внешних вызовов
    private OwnTransferRequestDto mapToOwnTransferDto(OwnTransfer model) {
        return OwnTransferRequestDto.builder()
                .fromAccountId(model.getFromAccountId())
                .toAccountId(model.getToAccountId())
                .amount(model.getAmount())
                .build();
    }

    private OtherTransferRequestDto mapToOtherTransferDto(OtherTransfer model) {
        return OtherTransferRequestDto.builder()
                .fromAccountId(model.getFromAccountId())
                .toCurrency(model.getToCurrency())
                .recipientEmail(model.getRecipientEmail())
                .amount(model.getAmount())
                .build();
    }

    // Создание результатов
    private TransferResult createSuccessResult(UUID fromAccountId, UUID toAccountId,
                                               BigDecimal amount, String message) {
        return TransferResult.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .status(TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .message(message)
                .build();
    }

    private TransferResult createSuccessResult(UUID fromAccountId, BigDecimal amount,
                                               String toCurrency, String recipientEmail, String message) {
        return TransferResult.builder()
                .fromAccountId(fromAccountId)
                .toCurrency(toCurrency)
                .recipientEmail(recipientEmail)
                .amount(amount)
                .status(TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .message(message)
                .build();
    }

    private TransferResult createFailedResult(UUID fromAccountId, UUID toAccountId,
                                              BigDecimal amount, String message, TransferErrorCode errorCode) {
        return TransferResult.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .status(TransferStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}