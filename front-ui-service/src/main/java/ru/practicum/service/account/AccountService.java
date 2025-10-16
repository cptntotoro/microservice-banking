package ru.practicum.service.account;

import reactor.core.publisher.Mono;
import ru.practicum.client.account.dto.AccountRequestDto;
import ru.practicum.model.account.Account;
import ru.practicum.model.user.UserWithAccounts;

import java.util.UUID;

/**
 * Сервис управления счетами
 */
public interface AccountService {

    /**
     * Получить пользователя с его счетами
     *
     * @param userId Идентификатор пользователя
     * @return Пользователь с его счетами
     */
    Mono<UserWithAccounts> getUserWithAccounts(UUID userId);

    /**
     * Создать счет
     *
     * @param userId Идентификатор пользователя
     * @param currencyCode Код валюты
     * @return Счет
     */
    Mono<Account> createAccount(UUID userId, String currencyCode);

    /**
     * Удалить счет
     *
     * @param userId Идентификатор пользователя
     * @param accountId Идентификатор счета
     */
    Mono<Void> deleteAccount(UUID userId, UUID accountId);

    /**
     * Получить счет по идентификатору пользователя и коду валюты
     *
     * @param accountRequestDto DTO запроса на добавление счета в новой валюте
     * @return Счет
     */
    Mono<Account> getAccount(AccountRequestDto accountRequestDto);
}