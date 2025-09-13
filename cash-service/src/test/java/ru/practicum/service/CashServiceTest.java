package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.client.account.AccountsServiceClient;
import ru.practicum.client.account.BalanceUpdateRequestDto;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.notification.NotificationRequestDto;
import ru.practicum.client.notification.NotificationsServiceClient;
import ru.practicum.dao.CashOperationDao;
import ru.practicum.model.CashRequest;
import ru.practicum.model.CashResponse;
import ru.practicum.repository.CashOperationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CashServiceTest {

    @Mock
    private AccountsServiceClient accountsServiceClient;

    @Mock
    private BlockerServiceClient blockerServiceClient;

    @Mock
    private NotificationsServiceClient notificationsServiceClient;

    @Mock
    private CashOperationRepository cashOperationRepository;

    @InjectMocks
    private CashServiceImpl cashService;

    private CashRequest validRequest;
    private CashOperationDao operation;

    @BeforeEach
    void setUp() {
        validRequest = new CashRequest();
        validRequest.setAccountId(UUID.randomUUID());
        validRequest.setUserId(UUID.randomUUID());
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setCurrency("USD");

        operation = CashOperationDao.builder()
                .operationUuid(UUID.randomUUID())
                .accountId(validRequest.getAccountId())
                .type("DEPOSIT")
                .amount(validRequest.getAmount())
                .currencyCode(validRequest.getCurrency())
                .status("PENDING")
                .description("Пополнение на сумму " + validRequest.getAmount() + " " + validRequest.getCurrency())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void deposit_successful() {
        // Arrange
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));
        when(blockerServiceClient.isOperationBlocked(any(CashRequest.class))).thenReturn(Mono.just(false));
        when(accountsServiceClient.verifyAccount(validRequest.getAccountId(), validRequest.getUserId())).thenReturn(Mono.just(true));
        when(accountsServiceClient.updateAccountBalance(any(BalanceUpdateRequestDto.class))).thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(Mono.empty());
        when(cashOperationRepository.findById(any(UUID.class))).thenReturn(Mono.just(operation));
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));

        // Act
        Mono<CashResponse> result = cashService.deposit(validRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("SUCCESS") &&
                                response.getMessage().equals("Пополнение успешно выполнено"))
                .verifyComplete();

        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
        verify(blockerServiceClient, times(1)).isOperationBlocked(validRequest);
        verify(accountsServiceClient, times(1)).verifyAccount(validRequest.getAccountId(), validRequest.getUserId());
        verify(accountsServiceClient, times(1)).updateAccountBalance(any(BalanceUpdateRequestDto.class));
        verify(notificationsServiceClient, times(1)).sendNotification(any(NotificationRequestDto.class));
        verify(cashOperationRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void deposit_blockedByBlockerService() {
        // Arrange
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));
        when(blockerServiceClient.isOperationBlocked(any(CashRequest.class))).thenReturn(Mono.just(true));
        when(cashOperationRepository.findById(any(UUID.class))).thenReturn(Mono.just(operation));
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));

        // Act
        Mono<CashResponse> result = cashService.deposit(validRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("BLOCKED") &&
                                response.getMessage().equals("Операция заблокирована службой безопасности"))
                .verifyComplete();

        verify(blockerServiceClient, times(1)).isOperationBlocked(validRequest);
        verify(accountsServiceClient, never()).verifyAccount(any(), any());
        verify(cashOperationRepository, times(1)).findById(any(UUID.class));
        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
    }

    @Test
    void deposit_invalidAccount() {
        // Arrange
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));
        when(blockerServiceClient.isOperationBlocked(any(CashRequest.class))).thenReturn(Mono.just(false));
        when(accountsServiceClient.verifyAccount(validRequest.getAccountId(), validRequest.getUserId())).thenReturn(Mono.just(false));
        when(cashOperationRepository.findById(any(UUID.class))).thenReturn(Mono.just(operation));
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));

        // Act
        Mono<CashResponse> result = cashService.deposit(validRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Неверный аккаунт или пользователь"))
                .verifyComplete();

        verify(accountsServiceClient, times(1)).verifyAccount(validRequest.getAccountId(), validRequest.getUserId());
        verify(accountsServiceClient, never()).updateAccountBalance(any());
        verify(cashOperationRepository, times(1)).findById(any(UUID.class));
        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
    }

    @Test
    void deposit_invalidRequest_nullAccountId() {
        // Arrange
        validRequest.setAccountId(null);
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));
        when(blockerServiceClient.isOperationBlocked(any(CashRequest.class))).thenReturn(Mono.just(false));
        when(cashOperationRepository.findById(any(UUID.class))).thenReturn(Mono.just(operation));
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));

        // Act
        Mono<CashResponse> result = cashService.deposit(validRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Идентификатор счета обязателен"))
                .verifyComplete();

        verify(cashOperationRepository, times(1)).findById(any(UUID.class));
        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
    }

    @Test
    void withdraw_successful() {
        // Arrange
        operation.setType("WITHDRAWAL");
        operation.setDescription("Снятие суммы " + validRequest.getAmount() + " " + validRequest.getCurrency());
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));
        when(blockerServiceClient.isOperationBlocked(any(CashRequest.class))).thenReturn(Mono.just(false));
        when(accountsServiceClient.verifyAccount(validRequest.getAccountId(), validRequest.getUserId())).thenReturn(Mono.just(true));
        when(accountsServiceClient.getAccountBalance(validRequest.getAccountId())).thenReturn(Mono.just(new BigDecimal("200.00")));
        when(accountsServiceClient.updateAccountBalance(any(BalanceUpdateRequestDto.class))).thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any(NotificationRequestDto.class))).thenReturn(Mono.empty());
        when(cashOperationRepository.findById(any(UUID.class))).thenReturn(Mono.just(operation));
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));

        // Act
        Mono<CashResponse> result = cashService.withdraw(validRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("SUCCESS") &&
                                response.getMessage().equals("Снятие успешно выполнено"))
                .verifyComplete();

        verify(accountsServiceClient, times(1)).getAccountBalance(validRequest.getAccountId());
        verify(accountsServiceClient, times(1)).updateAccountBalance(any(BalanceUpdateRequestDto.class));
        verify(notificationsServiceClient, times(1)).sendNotification(any(NotificationRequestDto.class));
        verify(cashOperationRepository, times(1)).findById(any(UUID.class));
        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
    }

    @Test
    void withdraw_insufficientFunds() {
        // Arrange
        operation.setType("WITHDRAWAL");
        operation.setDescription("Снятие суммы " + validRequest.getAmount() + " " + validRequest.getCurrency());
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));
        when(blockerServiceClient.isOperationBlocked(any(CashRequest.class))).thenReturn(Mono.just(false));
        when(accountsServiceClient.verifyAccount(validRequest.getAccountId(), validRequest.getUserId())).thenReturn(Mono.just(true));
        when(accountsServiceClient.getAccountBalance(validRequest.getAccountId())).thenReturn(Mono.just(new BigDecimal("50.00")));
        when(cashOperationRepository.findById(any(UUID.class))).thenReturn(Mono.just(operation));
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));

        // Act
        Mono<CashResponse> result = cashService.withdraw(validRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Недостаточно средств"))
                .verifyComplete();

        verify(accountsServiceClient, times(1)).getAccountBalance(validRequest.getAccountId());
        verify(accountsServiceClient, never()).updateAccountBalance(any());
        verify(cashOperationRepository, times(1)).findById(any(UUID.class));
        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
    }

    @Test
    void withdraw_blockedByBlockerService() {
        // Arrange
        operation.setType("WITHDRAWAL");
        operation.setDescription("Снятие суммы " + validRequest.getAmount() + " " + validRequest.getCurrency());
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));
        when(blockerServiceClient.isOperationBlocked(any(CashRequest.class))).thenReturn(Mono.just(true));
        when(cashOperationRepository.findById(any(UUID.class))).thenReturn(Mono.just(operation));
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));

        // Act
        Mono<CashResponse> result = cashService.withdraw(validRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("BLOCKED") &&
                                response.getMessage().equals("Операция заблокирована службой безопасности"))
                .verifyComplete();

        verify(blockerServiceClient, times(1)).isOperationBlocked(validRequest);
        verify(accountsServiceClient, never()).verifyAccount(any(), any());
        verify(cashOperationRepository, times(1)).findById(any(UUID.class));
        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
    }

    @Test
    void withdraw_invalidRequest_nullAmount() {
        // Arrange
        validRequest.setAmount(null);
        operation.setType("WITHDRAWAL");
        operation.setDescription("Снятие суммы null USD");
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));
        when(blockerServiceClient.isOperationBlocked(any(CashRequest.class))).thenReturn(Mono.just(false));
        when(cashOperationRepository.findById(any(UUID.class))).thenReturn(Mono.just(operation));
        when(cashOperationRepository.save(any(CashOperationDao.class))).thenReturn(Mono.just(operation));

        // Act
        Mono<CashResponse> result = cashService.withdraw(validRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Сумма должна быть положительной"))
                .verifyComplete();

        verify(cashOperationRepository, times(1)).findById(any(UUID.class));
        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
    }
}