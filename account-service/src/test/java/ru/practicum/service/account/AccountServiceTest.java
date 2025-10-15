//package ru.practicum.service.account;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//import ru.practicum.dao.account.AccountDao;
//import ru.practicum.exception.ErrorReasons;
//import ru.practicum.exception.NotFoundException;
//import ru.practicum.exception.ValidationException;
//import ru.practicum.mapper.account.AccountMapper;
//import ru.practicum.model.account.Account;
//import ru.practicum.model.currency.Currency;
//import ru.practicum.repository.account.AccountRepository;
//import ru.practicum.service.currency.CurrencyService;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.argThat;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class AccountServiceTest {
//
//    @Mock
//    private AccountRepository accountRepository;
//
//    @Mock
//    private AccountMapper accountMapper;
//
//    @Mock
//    private CurrencyService currencyService;
//
//    private AccountServiceImpl accountService;
//
//    private UUID userId;
//    private UUID accountId;
//    private UUID currencyId;
//    private String currencyCode;
//
//    @BeforeEach
//    void setUp() {
//        accountService = new AccountServiceImpl(accountRepository, accountMapper, currencyService);
//        userId = UUID.randomUUID();
//        accountId = UUID.randomUUID();
//        currencyId = UUID.randomUUID();
//        currencyCode = "USD";
//    }
//
//    @Test
//    void createAccount_shouldCreateSuccessfully() {
//        Account account = Account.builder()
//                .userId(userId)
//                .currencyCode(currencyCode)
//                .balance(BigDecimal.ZERO)
//                .build();
//
//        Currency currency = Currency.builder()
//                .id(currencyId)
//                .code(currencyCode)
//                .name("US Dollar")
//                .build();
//
//        AccountDao accountDao = AccountDao.builder()
//                .id(accountId)
//                .userId(userId)
//                .currencyId(currencyId)
//                .balance(BigDecimal.ZERO)
//                .build();
//
//        Account savedAccount = Account.builder()
//                .id(accountId)
//                .userId(userId)
//                .currencyCode(currencyCode)
//                .balance(BigDecimal.ZERO)
//                .build();
//
//        when(currencyService.getCurrencyByCode(currencyCode)).thenReturn(Mono.just(currency));
//        when(accountRepository.existsByUserIdAndCurrencyId(userId, currencyId)).thenReturn(Mono.just(false));
//        when(accountMapper.accountToAccountDao(account)).thenReturn(accountDao);
//        when(accountRepository.save(any(AccountDao.class))).thenReturn(Mono.just(accountDao));
//
//        // Настраиваем мок для получения валюты по ID после сохранения
//        when(currencyService.getCurrencyById(currencyId)).thenReturn(Mono.just(currency));
//        when(accountMapper.accountDaoToAccount(accountDao)).thenReturn(savedAccount);
//
//        StepVerifier.create(accountService.createAccount(account))
//                .expectNext(savedAccount)
//                .verifyComplete();
//
//        verify(accountRepository).save(argThat(dao ->
//                dao.getCreatedAt() != null && dao.getUpdatedAt() != null
//        ));
//    }
//
//    @Test
//    void createAccount_shouldThrowValidationExceptionIfDuplicate() {
//        Account account = Account.builder()
//                .userId(userId)
//                .currencyCode(currencyCode)
//                .build();
//
//        Currency currency = Currency.builder()
//                .id(currencyId)
//                .code(currencyCode)
//                .build();
//
//        when(currencyService.getCurrencyByCode(currencyCode)).thenReturn(Mono.just(currency));
//        when(accountRepository.existsByUserIdAndCurrencyId(userId, currencyId)).thenReturn(Mono.just(true));
//
//        StepVerifier.create(accountService.createAccount(account))
//                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
//                        throwable.getMessage().equals("У пользователя уже есть счет в этой валюте") &&
//                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.DUPLICATE_ENTITY))
//                .verify();
//    }
//
//    @Test
//    void getAccountById_shouldReturnAccount() {
//        AccountDao dao = AccountDao.builder().id(accountId).build();
//        Account account = Account.builder().id(accountId).build();
//
//        when(accountRepository.findById(accountId)).thenReturn(Mono.just(dao));
//        when(accountMapper.accountDaoToAccount(dao)).thenReturn(account);
//
//        StepVerifier.create(accountService.getAccountById(accountId))
//                .expectNext(account)
//                .verifyComplete();
//    }
//
//    @Test
//    void getAccountById_shouldThrowNotFoundException() {
//        when(accountRepository.findById(accountId)).thenReturn(Mono.empty());
//
//        StepVerifier.create(accountService.getAccountById(accountId))
//                .expectError(NotFoundException.class)
//                .verify();
//    }
//
//    @Test
//    void getUserAccounts_shouldReturnFluxOfAccounts() {
//        AccountDao dao1 = AccountDao.builder().id(UUID.randomUUID()).currencyId(currencyId).build();
//        AccountDao dao2 = AccountDao.builder().id(UUID.randomUUID()).currencyId(currencyId).build();
//
//        Account account1 = Account.builder().id(dao1.getId()).currencyCode(currencyCode).build();
//        Account account2 = Account.builder().id(dao2.getId()).currencyCode(currencyCode).build();
//
//        when(accountRepository.findByUserId(userId)).thenReturn(Flux.just(dao1, dao2));
//
//        Currency currency = Currency.builder()
//                .id(currencyId)
//                .code(currencyCode)
//                .build();
//        when(currencyService.getCurrencyById(currencyId)).thenReturn(Mono.just(currency));        when(accountMapper.accountDaoToAccount(dao1)).thenReturn(account1);
//        when(accountMapper.accountDaoToAccount(dao2)).thenReturn(account2);
//
//        StepVerifier.create(accountService.getUserAccounts(userId))
//                .expectNext(account1, account2)
//                .verifyComplete();
//    }
//
//    @Test
//    void deposit_shouldDepositSuccessfully() {
//        BigDecimal amount = BigDecimal.TEN;
//        AccountDao dao = AccountDao.builder()
//                .id(accountId)
//                .balance(BigDecimal.ZERO)
//                .build();
//
//        Account account = Account.builder()
//                .id(accountId)
//                .balance(amount)
//                .build();
//
//        when(accountRepository.findById(accountId)).thenReturn(Mono.just(dao));
//        when(accountRepository.save(any(AccountDao.class))).thenReturn(Mono.just(dao));
//        when(accountMapper.accountDaoToAccount(dao)).thenReturn(account);
//
//        StepVerifier.create(accountService.deposit(accountId, amount))
//                .expectNext(account)
//                .verifyComplete();
//
//        verify(accountRepository).save(argThat(savedDao ->
//                savedDao.getBalance().equals(amount) && savedDao.getUpdatedAt() != null
//        ));
//    }
//
//    @Test
//    void deposit_shouldThrowValidationExceptionIfAmountNegative() {
//        BigDecimal amount = BigDecimal.valueOf(-10);
//
//        StepVerifier.create(accountService.deposit(accountId, amount))
//                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
//                        throwable.getMessage().equals("Сумма пополнения должна быть больше нуля") &&
//                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.INVALID_AMOUNT))
//                .verify();
//    }
//
//    @Test
//    void withdraw_shouldWithdrawSuccessfully() {
//        BigDecimal amount = BigDecimal.valueOf(5);
//        AccountDao dao = AccountDao.builder()
//                .id(accountId)
//                .balance(BigDecimal.TEN)
//                .build();
//
//        Account account = Account.builder()
//                .id(accountId)
//                .balance(BigDecimal.valueOf(5))
//                .build();
//
//        when(accountRepository.findById(accountId)).thenReturn(Mono.just(dao));
//        when(accountRepository.save(any(AccountDao.class))).thenReturn(Mono.just(dao));
//        when(accountMapper.accountDaoToAccount(dao)).thenReturn(account);
//
//        StepVerifier.create(accountService.withdraw(accountId, amount))
//                .expectNext(account)
//                .verifyComplete();
//
//        verify(accountRepository).save(argThat(savedDao ->
//                savedDao.getBalance().equals(BigDecimal.valueOf(5)) && savedDao.getUpdatedAt() != null
//        ));
//    }
//
//    @Test
//    void withdraw_shouldThrowValidationExceptionIfInsufficientBalance() {
//        BigDecimal amount = BigDecimal.valueOf(15);
//        AccountDao dao = AccountDao.builder()
//                .id(accountId)
//                .balance(BigDecimal.TEN)
//                .build();
//
//        when(accountRepository.findById(accountId)).thenReturn(Mono.just(dao));
//
//        StepVerifier.create(accountService.withdraw(accountId, amount))
//                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
//                        throwable.getMessage().equals("Недостаточно средств на счете") &&
//                        ((ValidationException) throwable).getErrorCode().equals(ErrorReasons.INSUFFICIENT_BALANCE))
//                .verify();
//    }
//
//    @Test
//    void transferBetweenOwnAccounts_shouldTransferSuccessfully() {
//        UUID fromAccountId = UUID.randomUUID();
//        UUID toAccountId = UUID.randomUUID();
//        BigDecimal amount = BigDecimal.TEN;
//
//        AccountDao fromAccount = AccountDao.builder()
//                .id(fromAccountId)
//                .userId(userId)
//                .currencyId(currencyId)
//                .balance(BigDecimal.valueOf(20))
//                .build();
//
//        AccountDao toAccount = AccountDao.builder()
//                .id(toAccountId)
//                .userId(userId)
//                .currencyId(currencyId)
//                .balance(BigDecimal.ZERO)
//                .build();
//
//        when(accountRepository.findById(fromAccountId)).thenReturn(Mono.just(fromAccount));
//        when(accountRepository.findById(toAccountId)).thenReturn(Mono.just(toAccount));
//        when(accountRepository.save(any(AccountDao.class))).thenReturn(Mono.just(fromAccount));
//
//        StepVerifier.create(accountService.transferBetweenOwnAccounts(fromAccountId, toAccountId, amount))
//                .verifyComplete();
//
//        verify(accountRepository, times(2)).save(any(AccountDao.class));
//    }
//
//    @Test
//    void transferBetweenOwnAccounts_shouldThrowIfDifferentCurrencies() {
//        UUID fromAccountId = UUID.randomUUID();
//        UUID toAccountId = UUID.randomUUID();
//        BigDecimal amount = BigDecimal.TEN;
//
//        AccountDao fromAccount = AccountDao.builder()
//                .id(fromAccountId)
//                .userId(userId)
//                .currencyId(UUID.randomUUID()) // RUB
//                .balance(BigDecimal.valueOf(20))
//                .build();
//
//        AccountDao toAccount = AccountDao.builder()
//                .id(toAccountId)
//                .userId(userId)
//                .currencyId(UUID.randomUUID()) // USD
//                .balance(BigDecimal.ZERO)
//                .build();
//
//        Currency fromCurrency = Currency.builder().code("RUB").build();
//        Currency toCurrency = Currency.builder().code("USD").build();
//
//        when(accountRepository.findById(fromAccountId)).thenReturn(Mono.just(fromAccount));
//        when(accountRepository.findById(toAccountId)).thenReturn(Mono.just(toAccount));
//        when(currencyService.getCurrencyById(fromAccount.getCurrencyId())).thenReturn(Mono.just(fromCurrency));
//        when(currencyService.getCurrencyById(toAccount.getCurrencyId())).thenReturn(Mono.just(toCurrency));
//
//        StepVerifier.create(accountService.transferBetweenOwnAccounts(fromAccountId, toAccountId, amount))
//                .expectError(ValidationException.class)
//                .verify();
//    }
//}