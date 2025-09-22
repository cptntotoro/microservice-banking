package ru.practicum.service.cash;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.account.AccountServiceClient;
import ru.practicum.exception.transfer.TransferException;
import ru.practicum.model.cash.Cash;
import ru.practicum.model.transfer.TransferErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Реализация сервиса обналичивания денег.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {

    private final AccountServiceClient accountServiceClient;

    @Override
    public Mono<Cash> deposit(Cash model) {
        log.info("Processing deposit for account {}: amount {}", model.getAccountId(), model.getAmount());
        model.setOperationType("DEPOSIT");
        return validateAndProcess(model, true);
    }

    @Override
    public Mono<Cash> withdraw(Cash model) {
        log.info("Processing withdraw for account {}: amount {}", model.getAccountId(), model.getAmount());
        model.setOperationType("WITHDRAW");
        return validateAndProcess(model, false);
    }

    private Mono<Cash> validateAndProcess(Cash model, boolean isDeposit) {
        if (model.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new TransferException(
                    "Сумма операции должна быть положительной",
                    TransferErrorCode.INVALID_AMOUNT));
        }

        return accountServiceClient.getAccount(model.getAccountId())
                .flatMap(account -> {
                    if (!account.getUserId().equals(model.getUserId())) {
                        return Mono.error(new TransferException(
                                "Счет не принадлежит пользователю",
                                TransferErrorCode.ACCOUNT_OWNERSHIP_VIOLATION));
                    }

                    if (!isDeposit && account.getBalance().compareTo(model.getAmount()) < 0) {
                        return Mono.error(new TransferException(
                                "Недостаточно средств на счете",
                                TransferErrorCode.INSUFFICIENT_BALANCE));
                    }

                    if (!account.getCurrencyCode().equals(model.getCurrency())) {
                        return Mono.error(new TransferException(
                                "Валюта счета не совпадает",
                                TransferErrorCode.INVALID_AMOUNT));
                    }

                    return blockerClient.checkOperation(model.getAccountId(), model.getAmount(), model.getOperationType())
                            .flatMap(isAllowed -> {
                                if (!isAllowed) {
                                    return Mono.error(new TransferException(
                                            "Операция заблокирована",
                                            TransferErrorCode.BLOCKER_REJECTION));
                                }

                                BigDecimal newBalance = isDeposit
                                        ? account.getBalance().add(model.getAmount())
                                        : account.getBalance().subtract(model.getAmount());

                                return accountServiceClient.updateBalance(model.getAccountId(), newBalance)
                                        .flatMap(updated -> {
                                            model.setNewBalance(newBalance);
                                            model.setOperationDate(LocalDateTime.now());
                                            model.setStatus("SUCCESS");
                                            model.setMessage(isDeposit ? "Пополнение успешно" : "Снятие успешно");
                                            model.setOperationId(UUID.randomUUID());

                                            return notificationsClient.notifyUser(
                                                            model.getUserId(),
                                                            isDeposit ? "DEPOSIT_SUCCESS" : "WITHDRAW_SUCCESS",
                                                            model.getAmount())
                                                    .thenReturn(model);
                                        });
                            });
                });
    }
}