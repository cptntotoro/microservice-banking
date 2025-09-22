package ru.practicum.service.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.client.ExchangeServiceClient;
import ru.practicum.dao.account.AccountDao;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.exception.ErrorReasons;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.model.account.Account;
import ru.practicum.repository.account.AccountRepository;

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
     * Клиент для обращения к сервису обмена валют
     */
    private final ExchangeServiceClient exchangeServiceClient;

    @Override
    @Transactional
    public Mono<Account> createAccount(Account account) {
        return validateUniqueAccountForCurrency(account.getUserId(), account.getCurrencyCode())
                .then(Mono.defer(() -> {
                    AccountDao accountDao = accountMapper.accountToAccountDao(account);
                    accountDao.setCreatedAt(LocalDateTime.now());
                    accountDao.setUpdatedAt(LocalDateTime.now());
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
            // Если валюты разные, используем exchange-service для конвертации
            if (!fromAccount.getCurrencyId().equals(toAccount.getCurrencyId())) {
                return exchangeServiceClient.getRate(getCurrencyCode(fromAccount.getCurrencyId()), getCurrencyCode(toAccount.getCurrencyId()))
                        .flatMap(rate -> {
                            BigDecimal convertedAmount = amount.multiply(rate.getBuyRate());
                            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                            toAccount.setBalance(toAccount.getBalance().add(convertedAmount));
                            fromAccount.setUpdatedAt(LocalDateTime.now());
                            toAccount.setUpdatedAt(LocalDateTime.now());
                            return Mono.when(
                                    accountRepository.save(fromAccount),
                                    accountRepository.save(toAccount)
                            );
                        });
            }
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
    public Mono<Void> transferToOtherAccount(UUID fromAccountId, String toAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ValidationException(
                    "Сумма перевода должна быть больше нуля",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.INVALID_AMOUNT
            ));
        }
        return Mono.zip(
                accountRepository.findById(fromAccountId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет отправителя", fromAccountId.toString()))),
                accountRepository.findByAccountNumber(toAccountNumber)
                        .switchIfEmpty(Mono.error(new NotFoundException("Счет получателя с номером", toAccountNumber)))
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
            // Если валюты разные, используем exchange-service для конвертации
            if (!fromAccount.getCurrencyId().equals(toAccount.getCurrencyId())) {
                return exchangeServiceClient.getRate(getCurrencyCode(fromAccount.getCurrencyId()), getCurrencyCode(toAccount.getCurrencyId()))
                        .flatMap(rate -> {
                            BigDecimal convertedAmount = amount.multiply(rate.getBuyRate());
                            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                            toAccount.setBalance(toAccount.getBalance().add(convertedAmount));
                            fromAccount.setUpdatedAt(LocalDateTime.now());
                            toAccount.setUpdatedAt(LocalDateTime.now());
                            return Mono.when(
                                    accountRepository.save(fromAccount),
                                    accountRepository.save(toAccount)
                            );
                        });
            }
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            fromAccount.setUpdatedAt(LocalDateTime.now());
            toAccount.setUpdatedAt(LocalDateTime.now());
            return Mono.when(
                    accountRepository.save(fromAccount),
                    accountRepository.save(toAccount)
            );
        }).doOnSuccess(v -> log.info("Перевод {} со счета {} на счет {} выполнен", amount, fromAccountId, toAccountNumber));
    }

    /**
     * Получить актуальные курсы валют
     *
     * @return Список DTO курсов валют
     */
    public Flux<ExchangeRateDto> getCurrentExchangeRates() {
        return exchangeServiceClient.getCurrentRates()
                .doOnComplete(() -> log.info("Получены актуальные курсы валют"));
    }

//    /**
//     * Получить список доступных валют
//     *
//     * @return DTO доступных валют
//     */
//    public Mono<AvailableCurrenciesDto> getAvailableCurrencies() {
//        return exchangeServiceClient.getAvailableCurrencies()
//                .doOnSuccess(currencies -> log.info("Получены доступные валюты: {}", currencies));
//    }

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

    // Временный метод для получения кода валюты (должен быть заменен на реальный вызов к currencies таблице)
    private String getCurrencyCode(UUID currencyId) {
        // Заглушка: в реальной системе нужно получить код валюты из таблицы currencies
        return "USD"; // Заменить на реальный вызов, например, через CurrencyRepository
    }
}