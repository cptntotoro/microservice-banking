package ru.practicum.service.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.account.AccountDao;
import ru.practicum.dao.transaction.TransactionDao;
import ru.practicum.exception.ErrorReasons;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.mapper.transaction.TransactionMapper;
import ru.practicum.model.account.Account;
import ru.practicum.model.transaction.Transaction;
import ru.practicum.model.transaction.TransactionStatus;
import ru.practicum.model.transaction.TransactionType;
import ru.practicum.repository.account.AccountRepository;
import ru.practicum.repository.transaction.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Реализация сервиса для работы с транзакциями
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    /**
     * Репозиторий транзакций
     */
    private final TransactionRepository transactionRepository;

    /**
     * Репозиторий счетов
     */
    private final AccountRepository accountRepository;

    /**
     * Маппер транзакций
     */
    private final TransactionMapper transactionMapper;

    /**
     * Маппер счетов
     */
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public Mono<Void> transferMoney(Transaction transaction) {
        log.info("Перевод денег: счета {} -> {}, сумма: {}",
                transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount());

        return validateTransfer(transaction)
                .then(getAccountById(transaction.getFromAccountId()))
                .zipWith(getAccountById(transaction.getToAccountId()))
                .flatMap(accounts -> {
                    AccountDao fromAccount = accounts.getT1();
                    AccountDao toAccount = accounts.getT2();

                    if (fromAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
                        return Mono.error(new ValidationException(
                                "Недостаточно средств на счете отправителя",
                                HttpStatus.BAD_REQUEST,
                                ErrorReasons.INSUFFICIENT_FUNDS
                        ));
                    }

                    fromAccount.setBalance(fromAccount.getBalance().subtract(transaction.getAmount()));
                    toAccount.setBalance(toAccount.getBalance().add(transaction.getAmount()));

                    TransactionDao transactionDao = transactionMapper.transactionToTransactionDao(transaction);
                    transactionDao.setStatus(TransactionStatus.COMPLETED);
                    transactionDao.setCurrencyId(fromAccount.getCurrencyId());
                    transactionDao.setCreatedAt(LocalDateTime.now());

                    return accountRepository.save(fromAccount)
                            .then(accountRepository.save(toAccount))
                            .then(transactionRepository.save(transactionDao))
                            .then();
                })
                .doOnSuccess(v -> log.info("Перевод выполнен успешно"));
    }

    @Override
    @Transactional
    public Mono<Void> depositMoney(UUID accountId, Double amount, String description) {
        log.info("Пополнение счета: {}, сумма: {}", accountId, amount);

        BigDecimal depositAmount = BigDecimal.valueOf(amount);
        validateAmount(depositAmount);

        return getAccountById(accountId)
                .flatMap(account -> {
                    account.setBalance(account.getBalance().add(depositAmount));

                    Transaction transaction = Transaction.builder()
                            .type(TransactionType.DEPOSIT)
                            .toAccountId(accountId)
                            .amount(depositAmount)
                            .currencyId(account.getCurrencyId())
                            .description(description != null ? description : "Пополнение счета")
                            .status(TransactionStatus.COMPLETED)
                            .build();

                    TransactionDao transactionDao = transactionMapper.transactionToTransactionDao(transaction);
                    transactionDao.setCreatedAt(LocalDateTime.now());

                    return accountRepository.save(account)
                            .then(transactionRepository.save(transactionDao))
                            .then();
                })
                .doOnSuccess(v -> log.info("Счет {} пополнен на {}", accountId, amount));
    }

    @Override
    @Transactional
    public Mono<Void> withdrawMoney(UUID accountId, Double amount, String description) {
        log.info("Снятие со счета: {}, сумма: {}", accountId, amount);

        BigDecimal withdrawAmount = BigDecimal.valueOf(amount);
        validateAmount(withdrawAmount);

        return getAccountById(accountId)
                .flatMap(account -> {
                    if (account.getBalance().compareTo(withdrawAmount) < 0) {
                        return Mono.error(new ValidationException(
                                "Недостаточно средств на счете",
                                HttpStatus.BAD_REQUEST,
                                ErrorReasons.INSUFFICIENT_FUNDS
                        ));
                    }

                    account.setBalance(account.getBalance().subtract(withdrawAmount));

                    Transaction transaction = Transaction.builder()
                            .type(TransactionType.WITHDRAWAL)
                            .fromAccountId(accountId)
                            .amount(withdrawAmount)
                            .currencyId(account.getCurrencyId())
                            .description(description != null ? description : "Снятие со счета")
                            .status(TransactionStatus.COMPLETED)
                            .build();

                    TransactionDao transactionDao = transactionMapper.transactionToTransactionDao(transaction);
                    transactionDao.setCreatedAt(LocalDateTime.now());

                    return accountRepository.save(account)
                            .then(transactionRepository.save(transactionDao))
                            .then();
                })
                .doOnSuccess(v -> log.info("Со счета {} снято {}", accountId, amount));
    }

    @Override
    public Flux<Transaction> getAccountTransactions(UUID accountId) {
        log.info("Получение истории транзакций по счету: {}", accountId);

        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId)
                .map(transactionMapper::transactionDaoToTransaction)
                .flatMap(this::enrichTransactionWithAccountDetails);
    }

    @Override
    public Flux<Transaction> getUserTransactions(UUID userId) {
        log.info("Получение истории транзакций по пользователю: {}", userId);

        return accountRepository.findByUserId(userId)
                .flatMap(account -> transactionRepository.findByFromAccountIdOrToAccountId(
                        account.getId(), account.getId()))
                .map(transactionMapper::transactionDaoToTransaction)
                .flatMap(this::enrichTransactionWithAccountDetails)
                .distinct();
    }

    @Override
    public Mono<Transaction> getTransactionById(UUID transactionId) {
        log.info("Получение транзакции по ID: {}", transactionId);

        return transactionRepository.findById(transactionId)
                .map(transactionMapper::transactionDaoToTransaction)
                .flatMap(this::enrichTransactionWithAccountDetails)
                .switchIfEmpty(Mono.error(new NotFoundException("Транзакция", transactionId.toString())));
    }

    private Mono<Void> validateTransfer(Transaction transaction) {
        if (transaction.getFromAccountId() == null) {
            return Mono.error(new ValidationException(
                    "Счет отправителя обязателен",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.VALIDATION_ERROR
            ));
        }

        if (transaction.getToAccountId() == null) {
            return Mono.error(new ValidationException(
                    "Счет получателя обязателен",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.VALIDATION_ERROR
            ));
        }

        if (transaction.getFromAccountId().equals(transaction.getToAccountId())) {
            return Mono.error(new ValidationException(
                    "Нельзя переводить деньги на тот же счет",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.VALIDATION_ERROR
            ));
        }

        validateAmount(transaction.getAmount());

        return getAccountById(transaction.getFromAccountId())
                .zipWith(getAccountById(transaction.getToAccountId()))
                .flatMap(accounts -> {
                    AccountDao fromAccount = accounts.getT1();
                    AccountDao toAccount = accounts.getT2();

                    if (fromAccount == null) {
                        return Mono.error(new NotFoundException("Счет отправителя", transaction.getFromAccountId().toString()));
                    }
                    if (toAccount == null) {
                        return Mono.error(new NotFoundException("Счет получателя", transaction.getToAccountId().toString()));
                    }

                    return Mono.empty();
                });
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "Сумма должна быть положительной",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.VALIDATION_ERROR
            );
        }
    }

    private Mono<AccountDao> getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new NotFoundException("Счет", accountId.toString())));
    }

    /**
     * Обогащение транзакции деталями счетов
     */
    private Mono<Transaction> enrichTransactionWithAccountDetails(Transaction transaction) {
        Mono<Account> fromAccountMono = transaction.getFromAccountId() != null ?
                accountRepository.findById(transaction.getFromAccountId())
                        .map(accountMapper::accountDaoToAccount) :
                Mono.empty();

        Mono<Account> toAccountMono = transaction.getToAccountId() != null ?
                accountRepository.findById(transaction.getToAccountId())
                        .map(accountMapper::accountDaoToAccount) :
                Mono.empty();

        return Mono.zip(fromAccountMono.defaultIfEmpty(null), toAccountMono.defaultIfEmpty(null))
                .map(tuple -> {
                    // Можно добавить логику обогащения, если нужно
                    return transaction;
                });
    }
}