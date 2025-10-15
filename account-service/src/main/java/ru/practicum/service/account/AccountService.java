package ru.practicum.service.account;

import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.account.AccountRequestDto;
import ru.practicum.dto.account.AccountWithUserResponseDto;
import ru.practicum.dto.account.BalanceUpdateRequestDto;
import ru.practicum.dto.account.TransferDto;
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
    Mono<Account> getAccountWithUserByEmailAndCurrency(String email, String currency);

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

    Mono<Account> findAccountByUserAndCurrency(AccountRequestDto accountDto);

    /**
     * Проверить наличие ненулевого баланса на счете
     *
     * @param accountId Идентификатор счета
     * @return Да / Нет
     */
    Mono<Boolean> hasBalance(UUID accountId);

    Mono<Boolean> checkAndUpdateBalance(BalanceUpdateRequestDto balanceUpdateRequestDto);

//    /**
//     * Пополнить счет
//     *
//     * @param accountId Идентификатор счета
//     * @param amount    Сумма пополнения
//     * @return Обновленный счет
//     */
//    Mono<Account> deposit(UUID accountId, BigDecimal amount);

//    /**
//     * Снять деньги со счета
//     *
//     * @param accountId Идентификатор счета
//     * @param amount    Сумма снятия
//     * @return Обновленный счет
//     */
//    Mono<Account> withdraw(UUID accountId, BigDecimal amount);
//
//    /**
//     * Перевести деньги между счетами одного пользователя
//     *
//     * @param fromAccountId Идентификатор счета отправителя
//     * @param toAccountId   Идентификатор счета получателя
//     * @param amount        Сумма перевода
//     * @return Пустой результат
//     */
//    Mono<Void> transferBetweenOwnAccounts(UUID fromAccountId, UUID toAccountId, BigDecimal amount);
    Mono<Void> transferBetweenAccounts(TransferDto dto);

//    /**
//     * Перевести деньги на счет другого пользователя
//     *
//     * @param fromAccountId Идентификатор счета отправителя
//     * @param toAccountId   Идентификатор счета получателя
//     * @param amount        Сумма перевода
//     * @return Пустой результат
//     */
//    Mono<Void> transferToOtherAccount(UUID fromAccountId, UUID toAccountId, BigDecimal amount);

    Mono<Boolean> existsAccount(UUID userId, UUID accountId);

}