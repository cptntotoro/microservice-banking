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
import ru.practicum.model.currency.Currency;
import ru.practicum.repository.account.AccountRepository;
import ru.practicum.service.currency.CurrencyService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    /**
     * Сервис для работы с валютами
     */
    private final CurrencyService currencyService;

    @Override
    @Transactional
    public Mono<Account> createAccount(Account account) {
        account.setBalance(BigDecimal.ZERO);

        return validateUniqueAccountForCurrency(account.getUserId(), account.getCurrencyCode())
                .then(Mono.defer(() -> currencyService.getCurrencyByCode(account.getCurrencyCode())
                        .flatMap(currency -> {
                            AccountDao accountDao = accountMapper.accountToAccountDao(account);
                            accountDao.setCurrencyId(currency.getId());
                            accountDao.setCreatedAt(LocalDateTime.now());
                            accountDao.setUpdatedAt(LocalDateTime.now());
                            return accountRepository.save(accountDao);
                        })))
                .flatMap(savedAccountDao -> {
                    // После сохранения получаем полную информацию о валюте для маппинга
                    return currencyService.getCurrencyById(savedAccountDao.getCurrencyId())
                            .map(currency -> {
                                Account savedAccount = accountMapper.accountDaoToAccount(savedAccountDao);
                                savedAccount.setCurrencyCode(currency.getCode());
                                return savedAccount;
                            });
                })
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
                .flatMap(accountDao ->
                        currencyService.getCurrencyById(accountDao.getCurrencyId())
                                .map(currency -> {
                                    Account account = accountMapper.accountDaoToAccount(accountDao);
                                    account.setCurrencyCode(currency.getCode());
                                    return account;
                                })
                );
    }

    @Override
    @Transactional
    public Mono<Void> deleteAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())))
                .flatMap(account -> {
                    if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
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
                .map(account -> account.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Override
    @Transactional
    public Mono<Account> deposit(UUID accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ValidationException(
                    "Сумма пополнения должна быть больше нуля",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.INVALID_AMOUNT
            ));
        }
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())))
                .flatMap(accountDao -> {
                    accountDao.setBalance(accountDao.getBalance().add(amount));
                    accountDao.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(accountDao);
                })
                .map(accountMapper::accountDaoToAccount)
                .doOnSuccess(account -> log.info("Счет {} пополнен на сумму {}", accountId, amount));
    }

    @Override
    @Transactional
    public Mono<Account> withdraw(UUID accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ValidationException(
                    "Сумма снятия должна быть больше нуля",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.INVALID_AMOUNT
            ));
        }
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())))
                .flatMap(accountDao -> {
                    if (accountDao.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new ValidationException(
                                "Недостаточно средств на счете",
                                HttpStatus.BAD_REQUEST,
                                ErrorReasons.INSUFFICIENT_BALANCE
                        ));
                    }
                    accountDao.setBalance(accountDao.getBalance().subtract(amount));
                    accountDao.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(accountDao);
                })
                .map(accountMapper::accountDaoToAccount)
                .doOnSuccess(account -> log.info("Снято {} со счета {}", amount, accountId));
    }

    @Override
    @Transactional
    public Mono<Void> transferBetweenOwnAccounts(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ValidationException(
                    "Сумма перевода должна быть больше нуля",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.INVALID_AMOUNT
            ));
        }
        if (fromAccountId.equals(toAccountId)) {
            return Mono.error(new ValidationException(
                    "Счета отправителя и получателя не могут быть одинаковыми",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.INVALID_OPERATION
            ));
        }

        return Mono.zip(
                accountRepository.findById(fromAccountId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет отправителя", fromAccountId.toString()))),
                accountRepository.findById(toAccountId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет получателя", toAccountId.toString())))
        ).flatMap(tuple -> {
            AccountDao fromAccount = tuple.getT1();
            AccountDao toAccount = tuple.getT2();

            if (!fromAccount.getUserId().equals(toAccount.getUserId())) {
                return Mono.error(new ValidationException(
                        "Перевод возможен только между счетами одного пользователя",
                        HttpStatus.BAD_REQUEST,
                        ErrorReasons.INVALID_OPERATION
                ));
            }
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                return Mono.error(new ValidationException(
                        "Недостаточно средств на счете отправителя",
                        HttpStatus.BAD_REQUEST,
                        ErrorReasons.INSUFFICIENT_BALANCE
                ));
            }

            // Если валюты разные, возвращаем ошибку (конвертация временно не поддерживается)
            if (!fromAccount.getCurrencyId().equals(toAccount.getCurrencyId())) {
                return Mono.zip(
                        getCurrencyCode(fromAccount.getCurrencyId()),
                        getCurrencyCode(toAccount.getCurrencyId())
                ).flatMap(currencyCodes -> {
                    String fromCurrencyCode = currencyCodes.getT1();
                    String toCurrencyCode = currencyCodes.getT2();

                    return Mono.error(new ValidationException(
                            String.format("Конвертация между разными валютами временно не поддерживается. Перевод с %s на %s",
                                    fromCurrencyCode, toCurrencyCode),
                            HttpStatus.BAD_REQUEST,
                            ErrorReasons.INVALID_OPERATION
                    ));
                });
            }

            // Если валюты одинаковые, выполняем перевод
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            fromAccount.setUpdatedAt(LocalDateTime.now());
            toAccount.setUpdatedAt(LocalDateTime.now());
            return Mono.when(
                    accountRepository.save(fromAccount),
                    accountRepository.save(toAccount)
            );
        }).doOnSuccess(v -> log.info("Перевод {} между счетами {} и {} выполнен", amount, fromAccountId, toAccountId));
    }

    @Override
    @Transactional
    public Mono<Void> transferToOtherAccount(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ValidationException(
                    "Сумма перевода должна быть больше нуля",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.INVALID_AMOUNT
            ));
        }
        if (fromAccountId.equals(toAccountId)) {
            return Mono.error(new ValidationException(
                    "Счета отправителя и получателя не могут быть одинаковыми",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.INVALID_OPERATION
            ));
        }

        return Mono.zip(
                accountRepository.findById(fromAccountId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет отправителя", fromAccountId.toString()))),
                accountRepository.findById(toAccountId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет получателя", toAccountId.toString())))
        ).flatMap(tuple -> {
            AccountDao fromAccount = tuple.getT1();
            AccountDao toAccount = tuple.getT2();

            if (fromAccount.getUserId().equals(toAccount.getUserId())) {
                return Mono.error(new ValidationException(
                        "Перевод на собственный счет должен выполняться через transferBetweenOwnAccounts",
                        HttpStatus.BAD_REQUEST,
                        ErrorReasons.INVALID_OPERATION
                ));
            }
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                return Mono.error(new ValidationException(
                        "Недостаточно средств на счете отправителя",
                        HttpStatus.BAD_REQUEST,
                        ErrorReasons.INSUFFICIENT_BALANCE
                ));
            }

            // Если валюты разные, возвращаем ошибку (конвертация временно не поддерживается)
            if (!fromAccount.getCurrencyId().equals(toAccount.getCurrencyId())) {
                return Mono.zip(
                        getCurrencyCode(fromAccount.getCurrencyId()),
                        getCurrencyCode(toAccount.getCurrencyId())
                ).flatMap(currencyCodes -> {
                    String fromCurrencyCode = currencyCodes.getT1();
                    String toCurrencyCode = currencyCodes.getT2();

                    return Mono.error(new ValidationException(
                            String.format("Конвертация между разными валютами временно не поддерживается. Перевод с %s на %s",
                                    fromCurrencyCode, toCurrencyCode),
                            HttpStatus.BAD_REQUEST,
                            ErrorReasons.INVALID_OPERATION
                    ));
                });
            }

            // Если валюты одинаковые, выполняем перевод
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            fromAccount.setUpdatedAt(LocalDateTime.now());
            toAccount.setUpdatedAt(LocalDateTime.now());
            return Mono.when(
                    accountRepository.save(fromAccount),
                    accountRepository.save(toAccount)
            );
        }).doOnSuccess(v -> log.info("Перевод {} со счета {} на счет {} выполнен", amount, fromAccountId, toAccountId));
    }

    private Mono<Void> validateUniqueAccountForCurrency(UUID userId, String currencyCode) {
        // Сначала находим валюту по коду, затем проверяем существование счета
        return currencyService.getCurrencyByCode(currencyCode)
                .switchIfEmpty(Mono.error(new NotFoundException("Валюта", currencyCode)))
                .flatMap(currency ->
                        accountRepository.existsByUserIdAndCurrencyId(userId, currency.getId())
                )
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

    private Mono<String> getCurrencyCode(UUID currencyId) {
        return currencyService.getCurrencyById(currencyId)
                .map(Currency::getCode)
                .switchIfEmpty(Mono.error(new NotFoundException("Валюта", currencyId.toString())));
    }
}