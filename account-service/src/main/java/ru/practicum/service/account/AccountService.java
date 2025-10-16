package ru.practicum.service.account;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.account.AccountRequestDto;
import ru.practicum.dto.account.BalanceUpdateRequestDto;
import ru.practicum.dto.account.TransferDto;
import ru.practicum.model.account.Account;

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
     * Получить счет по идентификатору
     *
     * @param email    Адрес электронной почты
     * @param currency Код валюты
     * @return Счет
     */
    Mono<Account> getAccountByUserEmailAndCurrency(String email, String currency);

    /**
     * Получить счета пользователя
     *
     * @param userId Идентификатор пользователя
     * @return Список счетов
     */
    Flux<Account> getUserAccounts(UUID userId);

    /**
     * Удалить счет
     *
     * @param accountId Идентификатор счета
     */
    Mono<Void> deleteAccount(UUID accountId);

    /**
     * Получить счет по идентификатору пользователя и коду валюты
     *
     * @param accountDto DTO для создания счета
     * @return Счет
     */
    Mono<Account> findAccountByUserAndCurrency(AccountRequestDto accountDto);

    /**
     * Проверить и обновить баланс
     *
     * @param balanceUpdateRequestDto DTO запроса на обновление баланса
     * @return Да / Нет
     */
    Mono<Boolean> checkAndUpdateBalance(BalanceUpdateRequestDto balanceUpdateRequestDto);

    /**
     * Перевести средства между своими счетами
     *
     * @param dto DTO перевода
     */
    Mono<Void> transferBetweenAccounts(TransferDto dto);

    /**
     * Проверить существование счета пользователя
     *
     * @param userId    Идентификатор пользователя
     * @param accountId Идентификатор счета
     * @return Да / Нет
     */
    Mono<Boolean> existsAccount(UUID userId, UUID accountId);

}