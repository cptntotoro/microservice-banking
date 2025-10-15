package ru.practicum.service.account;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.account.AccountDao;
import ru.practicum.dto.account.AccountRequestDto;
import ru.practicum.dto.account.BalanceUpdateRequestDto;
import ru.practicum.dto.account.TransferDto;
import ru.practicum.exception.ErrorReasons;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.model.account.Account;
import ru.practicum.model.currency.Currency;
import ru.practicum.repository.account.AccountRepository;
import ru.practicum.service.currency.CurrencyService;
import ru.practicum.service.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Реализация сервиса для работы со счетами
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {
    /**
     * Репозиторий счетов
     */
    @Autowired
    private AccountRepository accountRepository;

    /**
     * Маппер счетов
     */
    @Autowired
    private AccountMapper accountMapper;

    /**
     * Сервис для работы с валютами
     */
    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private UserService userService;

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
                .flatMap(accountDao -> currencyService.getCurrencyById(accountDao.getCurrencyId())
                        .map(currency -> {
                            Account account = accountMapper.accountDaoToAccount(accountDao);
                            account.setCurrencyCode(currency.getCode());
                            return account;
                        }))
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())));
    }

    @Override
    public Mono<Account> getAccountWithUserByEmailAndCurrency(String email, String currencyCode) {
        return userService.getUserByEmail(email).flatMap(user -> currencyService.getCurrencyByCode(currencyCode)
                .flatMap(currency -> accountRepository.findByUserIdAndCurrencyId(user.getUuid(), currency.getId())
                        .map(accountDao -> {
                            Account account = accountMapper.accountDaoToAccount(accountDao);
                            account.setCurrencyCode(currency.getCode());
                            return account;
                        })))
                .switchIfEmpty(Mono.error(new NotFoundException("Счет по email и currencyCode", email + ";" + currencyCode)));
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
    public Mono<Account> findAccountByUserAndCurrency(AccountRequestDto accountDto) {
        return currencyService.getCurrencyByCode(accountDto.getCurrencyCode())
                .flatMap(currency -> accountRepository.findByUserIdAndCurrencyId(accountDto.getUserId(), currency.getId())
                        .map(accountMapper::accountDaoToAccount)
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountDto.toString()))));

    }

    @Override
    public Mono<Boolean> hasBalance(UUID accountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())))
                .map(account -> account.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Override
    @Transactional
    public Mono<Boolean> checkAndUpdateBalance(BalanceUpdateRequestDto balanceUpdateRequestDto) {
        if (balanceUpdateRequestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ValidationException(
                    "Сумма пополнения или снятия должна быть больше нуля",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.INVALID_AMOUNT
            ));
        }
        return accountRepository.findById(balanceUpdateRequestDto.getAccountId())
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", balanceUpdateRequestDto.getAccountId().toString())))
                .flatMap(accountDao -> {
                    if (!balanceUpdateRequestDto.isDeposit() && accountDao.getBalance().compareTo(balanceUpdateRequestDto.getAmount()) < 0) {
                        return Mono.error(new ValidationException(
                                "Сумма снятия должна быть больше суммы счета",
                                HttpStatus.BAD_REQUEST,
                                ErrorReasons.INVALID_AMOUNT
                        ));
                    }
                    accountDao.setBalance(accountDao.getBalance().add(balanceUpdateRequestDto.getAmount().multiply(BigDecimal.valueOf(balanceUpdateRequestDto.isDeposit() ? 1 : -1))));
                    accountDao.setUpdatedAt(LocalDateTime.now());
                    return accountRepository.save(accountDao);
                })
                .doOnSuccess(account -> log.info("Счет {} изменен на сумму {}", account.getId(), balanceUpdateRequestDto.getAmount()))
                .thenReturn(true);
    }

//    @Override
//    @Transactional
//    public Mono<Account> deposit(UUID accountId, BigDecimal amount) {
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            return Mono.error(new ValidationException(
//                    "Сумма пополнения должна быть больше нуля",
//                    HttpStatus.BAD_REQUEST,
//                    ErrorReasons.INVALID_AMOUNT
//            ));
//        }
//        return accountRepository.findById(accountId)
//                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())))
//                .flatMap(accountDao -> {
//                    accountDao.setBalance(accountDao.getBalance().add(amount));
//                    accountDao.setUpdatedAt(LocalDateTime.now());
//                    return accountRepository.save(accountDao);
//                })
//                .map(accountMapper::accountDaoToAccount)
//                .doOnSuccess(account -> log.info("Счет {} пополнен на сумму {}", accountId, amount));
//    }
//
//    @Override
//    @Transactional
//    public Mono<Account> withdraw(UUID accountId, BigDecimal amount) {
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            return Mono.error(new ValidationException(
//                    "Сумма снятия должна быть больше нуля",
//                    HttpStatus.BAD_REQUEST,
//                    ErrorReasons.INVALID_AMOUNT
//            ));
//        }
//        return accountRepository.findById(accountId)
//                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())))
//                .flatMap(accountDao -> {
//                    if (accountDao.getBalance().compareTo(amount) < 0) {
//                        return Mono.error(new ValidationException(
//                                "Недостаточно средств на счете",
//                                HttpStatus.BAD_REQUEST,
//                                ErrorReasons.INSUFFICIENT_BALANCE
//                        ));
//                    }
//                    accountDao.setBalance(accountDao.getBalance().subtract(amount));
//                    accountDao.setUpdatedAt(LocalDateTime.now());
//                    return accountRepository.save(accountDao);
//                })
//                .map(accountMapper::accountDaoToAccount)
//                .doOnSuccess(account -> log.info("Снято {} со счета {}", amount, accountId));
//    }

    @Override
    @Transactional
    public Mono<Void> transferBetweenAccounts(TransferDto dto) {
        return Mono.zip(
                accountRepository.findById(dto.getFromAccountId())
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет отправителя", dto.getFromAccountId().toString()))),
                accountRepository.findById(dto.getToAccountId())
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет получателя", dto.getToAccountId().toString())))
        ).flatMap(tuple -> {
            AccountDao fromAccount = tuple.getT1();
            AccountDao toAccount = tuple.getT2();

            fromAccount.setBalance(fromAccount.getBalance().subtract(dto.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(dto.getConvertedAmount()));
            fromAccount.setUpdatedAt(LocalDateTime.now());
            toAccount.setUpdatedAt(LocalDateTime.now());

            return Mono.when(
                    accountRepository.save(fromAccount),
                    accountRepository.save(toAccount)
            );
        }).doOnSuccess(v -> log.info("Перевод {} между счетами {} и {} выполнен", dto.getAmount(), dto.getFromAccountId(), dto.getToAccountId()));
    }

//    @Override
//    @Transactional
//    public Mono<Void> transferBetweenOwnAccounts(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            return Mono.error(new ValidationException(
//                    "Сумма перевода должна быть больше нуля",
//                    HttpStatus.BAD_REQUEST,
//                    ErrorReasons.INVALID_AMOUNT
//            ));
//        }
//        if (fromAccountId.equals(toAccountId)) {
//            return Mono.error(new ValidationException(
//                    "Счета отправителя и получателя не могут быть одинаковыми",
//                    HttpStatus.BAD_REQUEST,
//                    ErrorReasons.INVALID_OPERATION
//            ));
//        }
//
//        return Mono.zip(
//                accountRepository.findById(fromAccountId)
//                        .switchIfEmpty(Mono.error(new NotFoundException("Счет отправителя", fromAccountId.toString()))),
//                accountRepository.findById(toAccountId)
//                        .switchIfEmpty(Mono.error(new NotFoundException("Счет получателя", toAccountId.toString())))
//        ).flatMap(tuple -> {
//            AccountDao fromAccount = tuple.getT1();
//            AccountDao toAccount = tuple.getT2();
//
//            if (!fromAccount.getUserId().equals(toAccount.getUserId())) {
//                return Mono.error(new ValidationException(
//                        "Перевод возможен только между счетами одного пользователя",
//                        HttpStatus.BAD_REQUEST,
//                        ErrorReasons.INVALID_OPERATION
//                ));
//            }
//            if (fromAccount.getBalance().compareTo(amount) < 0) {
//                return Mono.error(new ValidationException(
//                        "Недостаточно средств на счете отправителя",
//                        HttpStatus.BAD_REQUEST,
//                        ErrorReasons.INSUFFICIENT_BALANCE
//                ));
//            }
//
//            // Если валюты разные, возвращаем ошибку (конвертация временно не поддерживается)
//            if (!fromAccount.getCurrencyId().equals(toAccount.getCurrencyId())) {
//                return Mono.zip(
//                        getCurrencyCode(fromAccount.getCurrencyId()),
//                        getCurrencyCode(toAccount.getCurrencyId())
//                ).flatMap(currencyCodes -> {
//                    String fromCurrencyCode = currencyCodes.getT1();
//                    String toCurrencyCode = currencyCodes.getT2();
//
//                    return Mono.error(new ValidationException(
//                            String.format("Конвертация между разными валютами временно не поддерживается. Перевод с %s на %s",
//                                    fromCurrencyCode, toCurrencyCode),
//                            HttpStatus.BAD_REQUEST,
//                            ErrorReasons.INVALID_OPERATION
//                    ));
//                });
//            }
//
//            // Если валюты одинаковые, выполняем перевод
//            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
//            toAccount.setBalance(toAccount.getBalance().add(amount));
//            fromAccount.setUpdatedAt(LocalDateTime.now());
//            toAccount.setUpdatedAt(LocalDateTime.now());
//            return Mono.when(
//                    accountRepository.save(fromAccount),
//                    accountRepository.save(toAccount)
//            );
//        }).doOnSuccess(v -> log.info("Перевод {} между счетами {} и {} выполнен", amount, fromAccountId, toAccountId));
//    }
//
//    @Override
//    @Transactional
//    public Mono<Void> transferToOtherAccount(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
//        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//            return Mono.error(new ValidationException(
//                    "Сумма перевода должна быть больше нуля",
//                    HttpStatus.BAD_REQUEST,
//                    ErrorReasons.INVALID_AMOUNT
//            ));
//        }
//        if (fromAccountId.equals(toAccountId)) {
//            return Mono.error(new ValidationException(
//                    "Счета отправителя и получателя не могут быть одинаковыми",
//                    HttpStatus.BAD_REQUEST,
//                    ErrorReasons.INVALID_OPERATION
//            ));
//        }
//
//        return Mono.zip(
//                accountRepository.findById(fromAccountId)
//                        .switchIfEmpty(Mono.error(new NotFoundException("Счет отправителя", fromAccountId.toString()))),
//                accountRepository.findById(toAccountId)
//                        .switchIfEmpty(Mono.error(new NotFoundException("Счет получателя", toAccountId.toString())))
//        ).flatMap(tuple -> {
//            AccountDao fromAccount = tuple.getT1();
//            AccountDao toAccount = tuple.getT2();
//
//            if (fromAccount.getUserId().equals(toAccount.getUserId())) {
//                return Mono.error(new ValidationException(
//                        "Перевод на собственный счет должен выполняться через transferBetweenOwnAccounts",
//                        HttpStatus.BAD_REQUEST,
//                        ErrorReasons.INVALID_OPERATION
//                ));
//            }
//            if (fromAccount.getBalance().compareTo(amount) < 0) {
//                return Mono.error(new ValidationException(
//                        "Недостаточно средств на счете отправителя",
//                        HttpStatus.BAD_REQUEST,
//                        ErrorReasons.INSUFFICIENT_BALANCE
//                ));
//            }
//
//            // Если валюты разные, возвращаем ошибку (конвертация временно не поддерживается)
//            if (!fromAccount.getCurrencyId().equals(toAccount.getCurrencyId())) {
//                return Mono.zip(
//                        getCurrencyCode(fromAccount.getCurrencyId()),
//                        getCurrencyCode(toAccount.getCurrencyId())
//                ).flatMap(currencyCodes -> {
//                    String fromCurrencyCode = currencyCodes.getT1();
//                    String toCurrencyCode = currencyCodes.getT2();
//
//                    return Mono.error(new ValidationException(
//                            String.format("Конвертация между разными валютами временно не поддерживается. Перевод с %s на %s",
//                                    fromCurrencyCode, toCurrencyCode),
//                            HttpStatus.BAD_REQUEST,
//                            ErrorReasons.INVALID_OPERATION
//                    ));
//                });
//            }
//
//            // Если валюты одинаковые, выполняем перевод
//            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
//            toAccount.setBalance(toAccount.getBalance().add(amount));
//            fromAccount.setUpdatedAt(LocalDateTime.now());
//            toAccount.setUpdatedAt(LocalDateTime.now());
//            return Mono.when(
//                    accountRepository.save(fromAccount),
//                    accountRepository.save(toAccount)
//            );
//        }).doOnSuccess(v -> log.info("Перевод {} со счета {} на счет {} выполнен", amount, fromAccountId, toAccountId));
//    }

    @Override
    public Mono<Boolean> existsAccount(UUID userId, UUID accountId) {
        return accountRepository.existsByUserIdAndId(userId, accountId);
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