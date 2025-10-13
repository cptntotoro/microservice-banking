package ru.practicum.service.account;

import reactor.core.publisher.Mono;
import ru.practicum.client.account.dto.AccountByEmailRequestDto;
import ru.practicum.client.account.dto.AccountRequestDto;
import ru.practicum.client.account.dto.AccountResponseDto;
import ru.practicum.model.account.Account;
import ru.practicum.model.user.UserWithAccounts;

import java.util.UUID;

/**
 * Сервис управления пользователями
 */
public interface AccountService {

    /**
     * Получить пользователя с его счетами
     *
     * @param userId Идентификатор пользователя
     * @return Пользователь с его счетами
     */
    Mono<UserWithAccounts> getUserWithAccounts(UUID userId);

    Mono<Account> createAccount(UUID userId, String currencyCode);

    Mono<Void> deleteAccount(UUID userId, UUID accountId);

    Mono<Account> getAccount(AccountRequestDto accountRequestDto);

    Mono<Account> getAccountByEmail(AccountByEmailRequestDto requestDto);
}