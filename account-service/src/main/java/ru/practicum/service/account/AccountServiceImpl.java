package ru.practicum.service.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.account.AccountDao;
import ru.practicum.exception.ErrorReasons;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.model.account.Account;
import ru.practicum.repository.account.AccountRepository;

import java.util.UUID;

/**
 * Реализация сервиса для работы со счетами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    /**
     * Репозиторий счетов
     */
    private final AccountRepository accountRepository;

    /**
     * Маппер счетов
     */
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public Mono<Account> createAccount(Account account) {
        return validateUniqueAccountForCurrency(account.getUserId(), account.getCurrencyId())
                .then(Mono.defer(() -> {
                    AccountDao accountDao = accountMapper.accountToAccountDao(account);
                    return accountRepository.save(accountDao);
                }))
                .map(accountMapper::accountDaoToAccount)
                .doOnSuccess(createdAccount -> log.info("Счет создан для пользователя: {}", createdAccount.getUserId()));
    }

    @Override
    public Mono<Account> getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(accountMapper::accountDaoToAccount)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())));
    }

    @Override
    public Flux<Account> getUserAccounts(UUID userId) {
        return accountRepository.findByUserId(userId)
                .map(accountMapper::accountDaoToAccount);
    }

    @Override
    public Mono<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(accountMapper::accountDaoToAccount)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет с номером", accountNumber)));
    }

    @Override
    @Transactional
    public Mono<Void> deleteAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())))
                .flatMap(account -> {
                    if (account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0) {
                        return Mono.error(new ValidationException(
                                "Невозможно удалить счет с ненулевым балансом",
                                HttpStatus.CONFLICT,
                                ErrorReasons.CONDITIONS_NOT_MET
                        ));
                    }
                    return accountRepository.deleteById(accountId);
                })
                .doOnSuccess(v -> log.info("Счет удален: {}", accountId));
    }

    @Override
    public Mono<Boolean> existsByUserAndCurrency(UUID userId, UUID currencyId) {
        return accountRepository.existsByUserIdAndCurrencyId(userId, currencyId);
    }

    @Override
    public Mono<Boolean> hasBalance(UUID accountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())))
                .map(account -> account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0);
    }

    private Mono<Void> validateUniqueAccountForCurrency(UUID userId, UUID currencyId) {
        return accountRepository.existsByUserIdAndCurrencyId(userId, currencyId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ValidationException(
                                "У пользователя уже есть счет в этой валюте",
                                HttpStatus.CONFLICT,
                                ErrorReasons.DUPLICATE_ENTITY
                        ));
                    }
                    return Mono.empty();
                });
    }
}