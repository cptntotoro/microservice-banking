package ru.practicum.service.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.transfer.TransferServiceClient;
import ru.practicum.client.transfer.dto.OwnTransferRequestClientDto;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.exception.transfer.TransferException;
import ru.practicum.mapper.transfer.TransferMapper;
import ru.practicum.model.transfer.OtherTransfer;
import ru.practicum.model.transfer.OwnTransfer;
import ru.practicum.model.transfer.TransferErrorCode;
import ru.practicum.model.transfer.TransferResult;
import ru.practicum.model.transfer.TransferStatus;
import ru.practicum.service.account.AccountService;
import ru.practicum.service.exchange.ExchangeRateService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {
    /**
     * Клиент для обращений к сервису переводов
     */
    private final TransferServiceClient transferServiceClient;

    /**
     * Маппер переводов средств
     */
    private final TransferMapper transferMapper;

    /**
     * Сервис курсов обмена валют
     */
    private final ExchangeRateService exchangeRateService;

    private final AccountService accountService;

    @Override
    public Mono<TransferResult> performOwnTransfer(OwnTransferRequestDto requestDto, UUID userId) {
        OwnTransferRequestClientDto clientDto = transferMapper.ownTransferRequestDtoToOwnTransferRequestClientDto(requestDto);
        clientDto.setUserId(userId);
        transferServiceClient.performOwnTransfer(clientDto)
                .flatMap()

        return validateCurrencies(model.getFromCurrency(), model.getToCurrency())
                .flatMap(valid -> {
                    if (!valid) {
                        log.warn("Недопустимая валюта для перевода: fromCurrency={}, toCurrency={}",
                                model.getFromCurrency(), model.getToCurrency());
                        return Mono.just(createFailedResult(
                                model.getFromAccountId(),
                                model.getToAccountId(),
                                model.getAmount(),
                                "Недопустимая валюта: " + model.getFromCurrency() + " или " + model.getToCurrency(),
                                TransferErrorCode.INVALID_CURRENCY));
                    }
                    log.info("Обработка перевода между своими счетами для пользователя {}: {} -> {}",
                            model.getUserId(), model.getFromAccountId(), model.getToAccountId());



                    OwnTransferRequestDto dto = transferMapper.ownTransferToOwnTransferRequestDto(model);
                    return transferServiceClient.performOwnTransfer(
                                    , model.getUserId())
                            .map(v -> createSuccessResult(
                                    model.getFromAccountId(),
                                    model.getToAccountId(),
                                    model.getAmount(),
                                    "Перевод успешно выполнен"))
                            .onErrorResume(TransferException.class, e -> {
                                log.warn("Ошибка перевода между своими счетами: {}", e.getMessage());
                                return Mono.just(createFailedResult(
                                        model.getFromAccountId(),
                                        model.getToAccountId(),
                                        model.getAmount(),
                                        e.getMessage(),
                                        e.getErrorCode()));
                            })
                            .onErrorResume(ServiceUnavailableException.class, e ->
                                    createServiceUnavailableResult(model, e, "Сервис переводов недоступен")) // Removed Mono.just
                            .onErrorResume(e -> {
                                log.error("Неизвестная ошибка при переводе между своими счетами для пользователя {}: {}",
                                        model.getUserId(), e.getMessage());
                                return Mono.just(createFailedResult(
                                        model.getFromAccountId(),
                                        model.getToAccountId(),
                                        model.getAmount(),
                                        "Неизвестная ошибка: " + e.getMessage(),
                                        TransferErrorCode.UNKNOWN_ERROR));
                            });
                });
    }

    @Override
    public Mono<TransferResult> performOtherTransfer(OtherTransfer model) {
//        accountService.getAccountByEmail(model.get(), model.)
        return validateCurrencies(model.getFromCurrency(), model.getToCurrency())
                .flatMap(valid -> {
                    if (!valid) {
                        log.warn("Недопустимая валюта для перевода: fromCurrency={}, toCurrency={}",
                                model.getFromCurrency(), model.getToCurrency());
                        return Mono.just(createFailedResult(
                                model.getFromAccountId(),
                                null,
                                model.getAmount(),
                                "Недопустимая валюта: " + model.getFromCurrency() + " или " + model.getToCurrency(),
                                TransferErrorCode.INVALID_CURRENCY));
                    }
                    log.info("Обработка перевода другому пользователю для пользователя {}: на email {}",
                            model.getUserId(), model.getRecipientEmail());

                    OtherTransferRequestDto dto = transferMapper.otherTransferToOtherTransferRequestDto(model);
                    return transferServiceClient.performOtherTransfer(
                                    transferMapper.otherTransferRequestDtoToOtherTransferRequestClientDto(dto), model.getUserId())
                            .map(v -> createSuccessResult(
                                    model.getFromAccountId(),
                                    model.getAmount(),
                                    model.getToCurrency(),
                                    model.getRecipientEmail(),
                                    "Перевод успешно выполнен"))
                            .onErrorResume(TransferException.class, e -> {
                                log.warn("Ошибка перевода другому пользователю: {}", e.getMessage());
                                return Mono.just(createFailedResult(
                                        model.getFromAccountId(),
                                        null,
                                        model.getAmount(),
                                        e.getMessage(),
                                        e.getErrorCode()));
                            })
                            .onErrorResume(ServiceUnavailableException.class, e ->
                                    createServiceUnavailableResult(model, e, "Сервис переводов недоступен")) // Removed Mono.just
                            .onErrorResume(e -> {
                                log.error("Неизвестная ошибка при переводе другому пользователю для пользователя {}: {}",
                                        model.getUserId(), e.getMessage());
                                return Mono.just(createFailedResult(
                                        model.getFromAccountId(),
                                        null,
                                        model.getAmount(),
                                        "Неизвестная ошибка: " + e.getMessage(),
                                        TransferErrorCode.UNKNOWN_ERROR));
                            });
                });
    }

    // Вспомогательный метод для валидации валют
    private Mono<Boolean> validateCurrencies(String fromCurrency, String toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            return Mono.just(false);
        }
        return exchangeRateService.getAvailableCurrencies()
                .collectList()
                .map(currencies -> currencies.contains(fromCurrency) && currencies.contains(toCurrency))
                .defaultIfEmpty(false)
                .doOnNext(valid -> log.debug("Валидация валют fromCurrency={}, toCurrency={}: {}",
                        fromCurrency, toCurrency, valid));
    }

    /**
     * Создание результата недоступности сервиса для перевода между своими счетами
     */
    private Mono<TransferResult> createServiceUnavailableResult(OwnTransfer model, ServiceUnavailableException e, String baseMessage) {
        String detailedMessage = String.format("%s (%s: %s)", baseMessage, e.getServiceId(), e.getServiceIssue());
        log.error("Сервис недоступен при переводе между своими счетами для пользователя {}: {}", model.getUserId(), detailedMessage);

        return Mono.just(createFailedResult(
                model.getFromAccountId(),
                model.getToAccountId(),
                model.getAmount(),
                detailedMessage,
                TransferErrorCode.TRANSFER_SERVICE_UNAVAILABLE));
    }

    /**
     * Создание результата недоступности сервиса для перевода другому пользователю
     */
    private Mono<TransferResult> createServiceUnavailableResult(OtherTransfer model, ServiceUnavailableException e, String baseMessage) {
        String detailedMessage = String.format("%s (%s: %s)", baseMessage, e.getServiceId(), e.getServiceIssue());
        log.error("Сервис недоступен при переводе другому пользователю для пользователя {}: {}", model.getUserId(), detailedMessage);

        return Mono.just(createFailedResult(
                model.getFromAccountId(),
                null,
                model.getAmount(),
                detailedMessage,
                TransferErrorCode.TRANSFER_SERVICE_UNAVAILABLE));
    }

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