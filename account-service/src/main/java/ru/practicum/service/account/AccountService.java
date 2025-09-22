package ru.practicum.service.account;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.account.Account;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сервис управления счетами
 */
public interface AccountService {

    /**
     * Создать счет
     *
     * @param account Счет
     * @return Созданный счет
     */
    Mono<Account> createAccount(Account account);

    /**
     * Получить счет по идентификатору
     *
     * @param accountId Идентификатор счета
     * @return Счет
     */
    Mono<Account> getAccountById(UUID accountId);

    /**
     * Получить счета пользователя
     *
     * @param userId Идентификатор пользователя
     * @return Список счетов
     */
    Flux<Account> getUserAccounts(UUID userId);

    /**
     * Получить счет по номеру
     *
     * @param accountNumber Номер счета
     * @return Счет
     */
    Mono<Account> getAccountByNumber(String accountNumber);

    /**
     * Удалить счет
     *
     * @param accountId Идентификатор счета
     * @return Пустой результат
     */
    Mono<Void> deleteAccount(UUID accountId);

    /**
     * Проверить наличие счета у пользователя в указанной валюте
     *
     * @param userId     Идентификатор пользователя
     * @param currencyId Идентификатор валюты
     * @return Да / Нет
     */
    Mono<Boolean> existsByUserAndCurrency(UUID userId, UUID currencyId);

    /**
     * Проверить наличие ненулевого баланса на счете
     *
     * @param accountId Идентификатор счета
     * @return Да / Нет
     */
    Mono<Boolean> hasBalance(UUID accountId);

    /**
     * Пополнить счет
     *
     * @param accountId Идентификатор счета
     * @param amount    Сумма пополнения
     * @return Обновленный счет
     */
    Mono<Account> deposit(UUID accountId, BigDecimal amount);

    /**
     * Снять деньги со счета
     *
     * @param accountId Идентификатор счета
     * @param amount    Сумма снятия
     * @return Обновленный счет
     */
    Mono<Account> withdraw(UUID accountId, BigDecimal amount);

    /**
     * Перевести деньги между счетами одного пользователя
     *
     * @param fromAccountId Идентификатор счета отправителя
     * @param toAccountId   Идентификатор счета получателя
     * @param amount        Сумма перевода
     * @return Пустой результат
     */
    Mono<Void> transferBetweenOwnAccounts(UUID fromAccountId, UUID toAccountId, BigDecimal amount);

    /**
     * Перевести деньги на счет другого пользователя
     *
     * @param fromAccountId Идентификатор счета отправителя
     * @param toAccountNumber Номер счета получателя
     * @param amount        Сумма перевода
     * @return Пустой результат
     */
    Mono<Void> transferToOtherAccount(UUID fromAccountId, String toAccountNumber, BigDecimal amount);
}