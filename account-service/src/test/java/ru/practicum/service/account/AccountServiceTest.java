package ru.practicum.service.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.client.ExchangeServiceClient;
import ru.practicum.dao.account.AccountDao;
import ru.practicum.dto.exchange.AvailableCurrenciesDto;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.exception.ErrorReasons;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.model.account.Account;
import ru.practicum.repository.account.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ExchangeServiceClient exchangeServiceClient;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountRepository, accountMapper, exchangeServiceClient);
    }

    @Test
    void createAccount_shouldCreateSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        Account account = Account.builder()
                .userId(userId)
                .currencyCode(currencyId)
                .build();

        AccountDao dao = new AccountDao();
        dao.setUserId(userId);
        dao.setCurrencyId(currencyId);

        when(accountRepository.existsByUserIdAndCurrencyId(userId, currencyId)).thenReturn(Mono.just(false));
        when(accountMapper.accountToAccountDao(account)).thenReturn(dao);
        when(accountRepository.save(any(AccountDao.class))).thenReturn(Mono.just(dao));
        when(accountMapper.accountDaoToAccount(dao)).thenReturn(account);

        StepVerifier.create(accountService.createAccount(account))
                .expectNext(account)
                .verifyComplete();

        verify(accountRepository).save(argThat(savedDao -> savedDao.getCreatedAt() != null && savedDao.getUpdatedAt() != null));
    }

    @Test
    void createAccount_shouldThrowValidationExceptionIfDuplicate() {
        UUID userId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        Account account = Account.builder()
                .userId(userId)
                .currencyCode(currencyId)
                .build();

        when(accountRepository.existsByUserIdAndCurrencyId(userId, currencyId)).thenReturn(Mono.just(true));

        StepVerifier.create(accountService.createAccount(account))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("У пользователя уже есть счет в этой валюте") &&
                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.DUPLICATE_ENTITY))
                .verify();
    }

    @Test
    void getAccountById_shouldReturnAccount() {
        UUID accountId = UUID.randomUUID();
        AccountDao dao = new AccountDao();
        Account account = new Account();

        when(accountRepository.findById(accountId)).thenReturn(Mono.just(dao));
        when(accountMapper.accountDaoToAccount(dao)).thenReturn(account);

        StepVerifier.create(accountService.getAccountById(accountId))
                .expectNext(account)
                .verifyComplete();
    }

    @Test
    void getAccountById_shouldThrowNotFoundException() {
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.getAccountById(accountId))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getUserAccounts_shouldReturnFluxOfAccounts() {
        UUID userId = UUID.randomUUID();
        AccountDao dao1 = new AccountDao();
        AccountDao dao2 = new AccountDao();
        Account account1 = new Account();
        Account account2 = new Account();

        when(accountRepository.findByUserId(userId)).thenReturn(Flux.just(dao1, dao2));
        when(accountMapper.accountDaoToAccount(dao1)).thenReturn(account1);
        when(accountMapper.accountDaoToAccount(dao2)).thenReturn(account2);

        StepVerifier.create(accountService.getUserAccounts(userId))
                .expectNext(account1, account2)
                .verifyComplete();
    }

    @Test
    void getAccountByNumber_shouldReturnAccount() {
        String accountNumber = "123456";
        AccountDao dao = new AccountDao();
        Account account = new Account();

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Mono.just(dao));
        when(accountMapper.accountDaoToAccount(dao)).thenReturn(account);

        StepVerifier.create(accountService.getAccountByNumber(accountNumber))
                .expectNext(account)
                .verifyComplete();
    }

    @Test
    void getAccountByNumber_shouldThrowNotFoundException() {
        String accountNumber = "123456";

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.getAccountByNumber(accountNumber))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void deleteAccount_shouldDeleteIfBalanceZero() {
        UUID accountId = UUID.randomUUID();
        AccountDao dao = new AccountDao();
        dao.setBalance(BigDecimal.ZERO);

        when(accountRepository.findById(accountId)).thenReturn(Mono.just(dao));
        when(accountRepository.deleteById(accountId)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.deleteAccount(accountId))
                .verifyComplete();
    }

    @Test
    void deleteAccount_shouldThrowValidationExceptionIfBalanceNotZero() {
        UUID accountId = UUID.randomUUID();
        AccountDao dao = new AccountDao();
        dao.setBalance(BigDecimal.ONE);

        when(accountRepository.findById(accountId)).thenReturn(Mono.just(dao));

        StepVerifier.create(accountService.deleteAccount(accountId))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Невозможно удалить счет с ненулевым балансом") &&
                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.CONDITIONS_NOT_MET))
                .verify();
    }

    @Test
    void deposit_shouldDepositSuccessfully() {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.TEN;
        AccountDao dao = new AccountDao();
        dao.setBalance(BigDecimal.ZERO);
        Account account = new Account();

        when(accountRepository.findById(accountId)).thenReturn(Mono.just(dao));
        when(accountRepository.save(any(AccountDao.class))).thenReturn(Mono.just(dao));
        when(accountMapper.accountDaoToAccount(dao)).thenReturn(account);

        StepVerifier.create(accountService.deposit(accountId, amount))
                .expectNext(account)
                .verifyComplete();

        verify(accountRepository).save(argThat(savedDao -> savedDao.getBalance().equals(amount)));
    }

    @Test
    void deposit_shouldThrowValidationExceptionIfAmountNegative() {
        UUID accountId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(-10);

        StepVerifier.create(accountService.deposit(accountId, amount))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
                        throwable.getMessage().equals("Сумма пополнения должна быть больше нуля") &&
                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.INVALID_AMOUNT))
                .verify();
    }

    @Test
    void getCurrentExchangeRates_shouldReturnFluxOfRates() {
        ExchangeRateDto rate1 = new ExchangeRateDto();
        ExchangeRateDto rate2 = new ExchangeRateDto();

        when(exchangeServiceClient.getCurrentRates()).thenReturn(Flux.just(rate1, rate2));

        StepVerifier.create(accountService.getCurrentExchangeRates())
                .expectNext(rate1, rate2)
                .verifyComplete();
    }

    @Test
    void getAvailableCurrencies_shouldReturnAvailableCurrenciesDto() {
        AvailableCurrenciesDto dto = AvailableCurrenciesDto.builder()
                .currencies(List.of("USD", "EUR"))
                .build();

        when(exchangeServiceClient.getAvailableCurrencies()).thenReturn(Mono.just(dto));

        StepVerifier.create(accountService.getAvailableCurrencies())
                .expectNext(dto)
                .verifyComplete();
    }
}