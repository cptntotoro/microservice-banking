package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.client.account.AccountsServiceClient;
import ru.practicum.client.account.BalanceUpdateRequestDto;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.notification.NotificationRequestDto;
import ru.practicum.client.notification.NotificationsServiceClient;
import ru.practicum.dao.CashOperationDao;
import ru.practicum.model.CashRequest;
import ru.practicum.repository.CashOperationRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    private UUID operationId;

    @BeforeEach
    void setUp() {
        validRequest = CashRequest.builder()
                .accountId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();
        operationId = UUID.randomUUID();
    }

    @Test
    void deposit_ValidRequest_ReturnsSuccess() {
        CashOperationDao operation = CashOperationDao.builder()
                .operationUuid(operationId)
                .status("PENDING")
                .build();

        when(cashOperationRepository.save(any(CashOperationDao.class)))
                .thenReturn(Mono.just(operation));
        when(blockerServiceClient.checkOperation(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(false));
        when(accountsServiceClient.verifyAccount(any(), any()))
                .thenReturn(Mono.just(true));
        when(accountsServiceClient.updateAccountBalance(any(BalanceUpdateRequestDto.class)))
                .thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any(NotificationRequestDto.class)))
                .thenReturn(Mono.empty());
        when(cashOperationRepository.findById(any(UUID.class)))
                .thenReturn(Mono.just(operation));

        StepVerifier.create(cashService.deposit(validRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("SUCCESS") &&
                                response.getMessage().contains("DEPOSIT успешно завершен")
                )
                .verifyComplete();

        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
        verify(blockerServiceClient).checkOperation(any(), any(), any(), any(), any());
        verify(accountsServiceClient).verifyAccount(any(), any());
        verify(accountsServiceClient).updateAccountBalance(any(BalanceUpdateRequestDto.class));
        verify(notificationsServiceClient).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    void deposit_InvalidRequest_NullAccountId_ReturnsError() {
        CashRequest invalidRequest = CashRequest.builder()
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        lenient().when(cashOperationRepository.save(any(CashOperationDao.class)))
                .thenReturn(Mono.just(CashOperationDao.builder().build()));

        StepVerifier.create(cashService.deposit(invalidRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Требуется идентификатор счета")
                )
                .verifyComplete();
    }

    @Test
    void deposit_BlockedOperation_ReturnsBlocked() {
        CashOperationDao operation = CashOperationDao.builder()
                .operationUuid(operationId)
                .status("PENDING")
                .build();

        when(cashOperationRepository.save(any(CashOperationDao.class)))
                .thenReturn(Mono.just(operation));
        when(blockerServiceClient.checkOperation(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(true));
        when(cashOperationRepository.findById(any(UUID.class)))
                .thenReturn(Mono.just(operation));

        StepVerifier.create(cashService.deposit(validRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("BLOCKED") &&
                                response.getMessage().equals("Операция заблокирована службой безопасности")
                )
                .verifyComplete();

        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
        verify(blockerServiceClient).checkOperation(any(), any(), any(), any(), any());
        verifyNoInteractions(accountsServiceClient, notificationsServiceClient);
    }

    @Test
    void deposit_InvalidAccount_ReturnsError() {
        CashOperationDao operation = CashOperationDao.builder()
                .operationUuid(operationId)
                .status("PENDING")
                .build();

        when(cashOperationRepository.save(any(CashOperationDao.class)))
                .thenReturn(Mono.just(operation));
        when(blockerServiceClient.checkOperation(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(false));
        when(accountsServiceClient.verifyAccount(any(), any()))
                .thenReturn(Mono.just(false));
        when(cashOperationRepository.findById(any(UUID.class)))
                .thenReturn(Mono.just(operation));

        StepVerifier.create(cashService.deposit(validRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Неверный счет или пользователь")
                )
                .verifyComplete();

        verify(cashOperationRepository, times(3)).save(any(CashOperationDao.class));
        verify(blockerServiceClient).checkOperation(any(), any(), any(), any(), any());
        verify(accountsServiceClient).verifyAccount(any(), any());
        verifyNoInteractions(notificationsServiceClient);
    }

    @Test
    void withdraw_ValidRequest_ReturnsSuccess() {
        CashOperationDao operation = CashOperationDao.builder()
                .operationUuid(operationId)
                .status("PENDING")
                .build();

        when(cashOperationRepository.save(any(CashOperationDao.class)))
                .thenReturn(Mono.just(operation));
        when(blockerServiceClient.checkOperation(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(false));
        when(accountsServiceClient.verifyAccount(any(), any()))
                .thenReturn(Mono.just(true));
        when(accountsServiceClient.getAccountBalance(any()))
                .thenReturn(Mono.just(new BigDecimal("200.00")));
        when(accountsServiceClient.updateAccountBalance(any(BalanceUpdateRequestDto.class)))
                .thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any(NotificationRequestDto.class)))
                .thenReturn(Mono.empty());
        when(cashOperationRepository.findById(any(UUID.class)))
                .thenReturn(Mono.just(operation));

        StepVerifier.create(cashService.withdraw(validRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("SUCCESS") &&
                                response.getMessage().contains("WITHDRAWAL успешно завершен")
                )
                .verifyComplete();

        verify(cashOperationRepository, times(2)).save(any(CashOperationDao.class));
        verify(blockerServiceClient).checkOperation(any(), any(), any(), any(), any());
        verify(accountsServiceClient).verifyAccount(any(), any());
        verify(accountsServiceClient).getAccountBalance(any());
        verify(accountsServiceClient).updateAccountBalance(any(BalanceUpdateRequestDto.class));
        verify(notificationsServiceClient).sendNotification(any(NotificationRequestDto.class));
    }

    @Test
    void withdraw_InsufficientFunds_ReturnsError() {
        CashOperationDao operation = CashOperationDao.builder()
                .operationUuid(operationId)
                .status("PENDING")
                .build();

        when(cashOperationRepository.save(any(CashOperationDao.class)))
                .thenReturn(Mono.just(operation));
        when(blockerServiceClient.checkOperation(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(false));
        when(accountsServiceClient.verifyAccount(any(), any()))
                .thenReturn(Mono.just(true));
        when(accountsServiceClient.getAccountBalance(any()))
                .thenReturn(Mono.just(new BigDecimal("50.00")));
        when(cashOperationRepository.findById(any(UUID.class)))
                .thenReturn(Mono.just(operation));

        StepVerifier.create(cashService.withdraw(validRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Недостаточно средств")
                )
                .verifyComplete();

        verify(cashOperationRepository, times(3)).save(any(CashOperationDao.class));
        verify(blockerServiceClient).checkOperation(any(), any(), any(), any(), any());
        verify(accountsServiceClient).verifyAccount(any(), any());
        verify(accountsServiceClient).getAccountBalance(any());
        verifyNoInteractions(notificationsServiceClient);
    }

    @Test
    void withdraw_NullAmount_ReturnsError() {
        CashRequest invalidRequest = CashRequest.builder()
                .accountId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .currency("USD")
                .build();

        lenient().when(cashOperationRepository.save(any(CashOperationDao.class)))
                .thenReturn(Mono.just(CashOperationDao.builder().build()));

        StepVerifier.create(cashService.withdraw(invalidRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Сумма должна быть положительной")
                )
                .verifyComplete();
    }

    @Test
    void withdraw_EmptyCurrency_ReturnsError() {
        CashRequest invalidRequest = CashRequest.builder()
                .accountId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("")
                .build();

        lenient().when(cashOperationRepository.save(any(CashOperationDao.class)))
                .thenReturn(Mono.just(CashOperationDao.builder().build()));

        StepVerifier.create(cashService.withdraw(invalidRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("ERROR") &&
                                response.getMessage().equals("Требуется валюта")
                )
                .verifyComplete();
    }
}