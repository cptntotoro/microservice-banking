package ru.practicum.service.account;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.account.Account;

import java.util.UUID;

/**
 * Сервис для работы со счетами
 */
public interface AccountService {

    /**
     * Создание нового счета
     *
     * @param account Модель счета
     * @return Модель созданного счета
     */
    Mono<Account> createAccount(Account account);

    /**
     * Получение счета по идентификатору
     *
     * @param accountId Идентификатор счета
     * @return Модель счета
     */
    Mono<Account> getAccountById(UUID accountId);

    /**
     * Получение всех счетов пользователя
     *
     * @param userId Идентификатор пользователя
     * @return Список моделей счетов
     */
    Flux<Account> getUserAccounts(UUID userId);

    /**
     * Получение счета по номеру
     *
     * @param accountNumber Номер счета
     * @return Модель счета
     */
    Mono<Account> getAccountByNumber(String accountNumber);

    /**
     * Удаление счета
     *
     * @param accountId Идентификатор счета
     * @return Результат операции
     */
    Mono<Void> deleteAccount(UUID accountId);

    /**
     * Проверка существования счета у пользователя в валюте
     *
     * @param userId Идентификатор пользователя
     * @param currencyId Идентификатор валюты
     * @return true если счет существует
     */
    Mono<Boolean> existsByUserAndCurrency(UUID userId, UUID currencyId);

    /**
     * Проверка наличия средств на счете
     *
     * @param accountId Идентификатор счета
     * @return true если на счете есть средства
     */
    Mono<Boolean> hasBalance(UUID accountId);
}
