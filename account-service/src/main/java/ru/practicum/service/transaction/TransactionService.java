package ru.practicum.service.transaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.transaction.Transaction;

import java.util.UUID;

/**
 * Сервис для работы с транзакциями
 */
public interface TransactionService {

    /**
     * Перевод денег между счетами
     *
     * @param transaction Модель транзакции
     * @return Результат операции
     */
    Mono<Void> transferMoney(Transaction transaction);

    /**
     * Пополнение счета
     *
     * @param accountId Идентификатор счета
     * @param amount Сумма пополнения
     * @param description Описание операции
     * @return Результат операции
     */
    Mono<Void> depositMoney(UUID accountId, Double amount, String description);

    /**
     * Снятие денег со счета
     *
     * @param accountId Идентификатор счета
     * @param amount Сумма снятия
     * @param description Описание операции
     * @return Результат операции
     */
    Mono<Void> withdrawMoney(UUID accountId, Double amount, String description);

    /**
     * Получение истории транзакций по счету
     *
     * @param accountId Идентификатор счета
     * @return Список транзакций
     */
    Flux<Transaction> getAccountTransactions(UUID accountId);

    /**
     * Получение истории транзакций по пользователю
     *
     * @param userId Идентификатор пользователя
     * @return Список транзакций
     */
    Flux<Transaction> getUserTransactions(UUID userId);

    /**
     * Получение транзакции по идентификатору
     *
     * @param transactionId Идентификатор транзакции
     * @return Транзакция
     */
    Mono<Transaction> getTransactionById(UUID transactionId);
}
