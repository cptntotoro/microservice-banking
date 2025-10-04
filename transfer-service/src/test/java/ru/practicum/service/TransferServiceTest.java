package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.client.account.dto.AccountResponseDto;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.client.account.dto.TransferDto;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.blocker.dto.OperationCheckRequestDto;
import ru.practicum.client.blocker.dto.OperationCheckResponseDto;
import ru.practicum.client.exchange.ExchangeServiceClient;
import ru.practicum.client.exchange.dto.ExchangeRequestDto;
import ru.practicum.client.exchange.dto.ExchangeResponseDto;
import ru.practicum.client.notification.NotificationsServiceClient;
import ru.practicum.client.notification.dto.NotificationRequestDto;
import ru.practicum.dao.TransferDao;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.TransferMapper;
import ru.practicum.model.OperationStatus;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;
import ru.practicum.model.TransferType;
import ru.practicum.repository.TransferRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private BlockerServiceClient blockerServiceClient;

    @Mock
    private NotificationsServiceClient notificationsServiceClient;

    @Mock
    private ExchangeServiceClient exchangeServiceClient;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferMapper transferMapper;

    @InjectMocks
    private TransferServiceImpl transferService;

    private UUID fromAccountId;
    private UUID toAccountId;
    private UUID userId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal convertedAmount;

    @BeforeEach
    void setUp() {
        fromAccountId = UUID.randomUUID();
        toAccountId = UUID.randomUUID();
        userId = UUID.randomUUID();
        fromCurrency = "USD";
        toCurrency = "EUR";
        amount = new BigDecimal("100.00");
        convertedAmount = new BigDecimal("85.00");
        // Сбрасываем моки перед каждым тестом
        Mockito.reset(accountServiceClient, blockerServiceClient, notificationsServiceClient, exchangeServiceClient, transferRepository, transferMapper);
    }

    private AccountResponseDto createAccountResponse(UUID id, String currencyCode, BigDecimal balance) {
        return AccountResponseDto.builder()
                .id(id)
                .currencyCode(currencyCode)
                .balance(balance)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ExchangeResponseDto createConvertResponse() {
        return ExchangeResponseDto.builder()
                .originalAmount(amount)
                .fromCurrency(fromCurrency)
                .convertedAmount(convertedAmount)
                .toCurrency(toCurrency)
                .exchangeRate(new BigDecimal("0.85"))
                .build();
    }

    private OperationCheckResponseDto createOperationCheckResponse(boolean blocked) {
        return OperationCheckResponseDto.builder()
                .blocked(blocked)
                .description(blocked ? "Подозрительная операция" : "Операция разрешена")
                .build();
    }

    @Test
    void transferBetweenOwnAccounts_shouldSuccess() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        AccountResponseDto fromAccount = createAccountResponse(fromAccountId, fromCurrency, new BigDecimal("200.00"));
        AccountResponseDto toAccount = createAccountResponse(toAccountId, toCurrency, new BigDecimal("50.00"));
        ExchangeResponseDto convertResponse = createConvertResponse();
        OperationCheckResponseDto checkResponse = createOperationCheckResponse(false);

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountById(toAccountId)).thenReturn(Mono.just(toAccount));
        when(blockerServiceClient.checkOperation(any(OperationCheckRequestDto.class))).thenReturn(Mono.just(checkResponse));
        ExchangeRequestDto exchangeRequestDto = ExchangeRequestDto.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount)
                .build();
        when(exchangeServiceClient.convertCurrency(exchangeRequestDto))
                .thenReturn(Mono.just(convertResponse));
        TransferDto transferDto = TransferDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();
        when(accountServiceClient.transferBetweenOwnAccounts(transferDto))
                .thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any())).thenReturn(Mono.empty());
        when(transferRepository.save(any())).thenReturn(Mono.empty());
        when(transferMapper.transferResponseToTransferDao(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals(OperationStatus.SUCCESS) &&
                                response.getFromAccountId().equals(fromAccountId) &&
                                response.getToAccountId().equals(toAccountId) &&
                                response.getAmount().equals(amount) &&
                                response.getConvertedAmount().equals(convertedAmount)
                )
                .verifyComplete();

        verify(accountServiceClient).getAccountById(fromAccountId);
        verify(accountServiceClient).getAccountById(toAccountId);
        verify(blockerServiceClient).checkOperation(any(OperationCheckRequestDto.class));
        verify(exchangeServiceClient).convertCurrency(exchangeRequestDto);
        verify(accountServiceClient).transferBetweenOwnAccounts(transferDto);
        verify(notificationsServiceClient).sendNotification(any());
    }

    @Test
    void transferBetweenOwnAccounts_shouldFailWhenAmountZero() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(BigDecimal.ZERO)
                .build();

        when(transferRepository.save(any())).thenReturn(Mono.empty());
        when(transferMapper.transferResponseToTransferDao(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void transferBetweenOwnAccounts_shouldFailWhenSameAccounts() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(fromAccountId)
                .amount(amount)
                .build();

        when(transferRepository.save(any())).thenReturn(Mono.empty());
        when(transferMapper.transferResponseToTransferDao(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void transferBetweenOwnAccounts_shouldFailWhenInsufficientFunds() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        AccountResponseDto fromAccount = createAccountResponse(fromAccountId, fromCurrency, new BigDecimal("50.00"));
        AccountResponseDto toAccount = createAccountResponse(toAccountId, toCurrency, new BigDecimal("100.00"));

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountById(toAccountId)).thenReturn(Mono.just(toAccount));
        when(transferRepository.save(any())).thenReturn(Mono.empty());
        when(transferMapper.transferResponseToTransferDao(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void transferBetweenOwnAccounts_shouldFailWhenSuspiciousOperation() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        AccountResponseDto fromAccount = createAccountResponse(fromAccountId, fromCurrency, new BigDecimal("200.00"));
        AccountResponseDto toAccount = createAccountResponse(toAccountId, toCurrency, new BigDecimal("50.00"));
        OperationCheckResponseDto checkResponse = createOperationCheckResponse(true);

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountById(toAccountId)).thenReturn(Mono.just(toAccount));
        when(blockerServiceClient.checkOperation(any(OperationCheckRequestDto.class))).thenReturn(Mono.just(checkResponse));
        when(transferRepository.save(any())).thenReturn(Mono.empty());
        when(transferMapper.transferResponseToTransferDao(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void transferBetweenOwnAccounts_shouldFailWhenAccountServiceUnavailable() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        ServiceUnavailableException exception = new ServiceUnavailableException("account-service", "Сервис недоступен");

        // Мок для fromAccountId - возвращаем ошибку
        when(accountServiceClient.getAccountById(fromAccountId))
                .thenReturn(Mono.error(exception));

        // Мок для toAccountId - возвращаем фиктивный аккаунт, чтобы избежать NPE (Mono.zip подписывается на оба источника)
        AccountResponseDto dummyToAccount = createAccountResponse(toAccountId, toCurrency, new BigDecimal("50.00"));
        when(accountServiceClient.getAccountById(toAccountId))
                .thenReturn(Mono.just(dummyToAccount));

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof ServiceUnavailableException &&
                                error.getMessage().contains("account-service") &&
                                error.getMessage().contains("Не удалось получить счет отправителя"))
                .verify();

        // Проверяем, что getAccountById вызван для обоих (из-за параллельной подписки Mono.zip)
        verify(accountServiceClient).getAccountById(fromAccountId);
        verify(accountServiceClient).getAccountById(toAccountId);
        // Проверяем, что другие сервисы и репозитории не вызывались
        verifyNoInteractions(exchangeServiceClient, blockerServiceClient, notificationsServiceClient, transferRepository, transferMapper);
    }

    @Test
    void transferBetweenOwnAccounts_shouldContinueWhenNotificationFails() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        AccountResponseDto fromAccount = createAccountResponse(fromAccountId, fromCurrency, new BigDecimal("200.00"));
        AccountResponseDto toAccount = createAccountResponse(toAccountId, toCurrency, new BigDecimal("50.00"));
        ExchangeResponseDto convertResponse = createConvertResponse();
        OperationCheckResponseDto checkResponse = createOperationCheckResponse(false);

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountById(toAccountId)).thenReturn(Mono.just(toAccount));
        when(blockerServiceClient.checkOperation(any(OperationCheckRequestDto.class))).thenReturn(Mono.just(checkResponse));
        ExchangeRequestDto exchangeRequestDto = ExchangeRequestDto.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount)
                .build();
        when(exchangeServiceClient.convertCurrency(exchangeRequestDto))
                .thenReturn(Mono.just(convertResponse));

        when(accountServiceClient.transferBetweenOwnAccounts(TransferDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build()))
                .thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any()))
                .thenReturn(Mono.error(new RuntimeException("Notification failed")));
        when(transferRepository.save(any())).thenReturn(Mono.empty());
        when(transferMapper.transferResponseToTransferDao(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatus().equals(OperationStatus.SUCCESS))
                .verifyComplete();

        verify(notificationsServiceClient).sendNotification(any());
    }

    @Test
    void transferToOtherAccount_shouldSuccess() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        UUID differentUserId = UUID.randomUUID();
        AccountResponseDto fromAccount = AccountResponseDto.builder()
                .id(fromAccountId)
                .currencyCode(fromCurrency)
                .balance(new BigDecimal("200.00"))
                .createdAt(LocalDateTime.now())
                .build();
        AccountResponseDto toAccount = AccountResponseDto.builder()
                .id(toAccountId)
                .currencyCode(toCurrency)
                .balance(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .build();

        ExchangeResponseDto convertResponse = createConvertResponse();
        OperationCheckResponseDto checkResponse = createOperationCheckResponse(false);

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountById(toAccountId)).thenReturn(Mono.just(toAccount));
        when(blockerServiceClient.checkOperation(any(OperationCheckRequestDto.class))).thenReturn(Mono.just(checkResponse));
        ExchangeRequestDto exchangeRequestDto = ExchangeRequestDto.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount)
                .build();
        when(exchangeServiceClient.convertCurrency(exchangeRequestDto))
                .thenReturn(Mono.just(convertResponse));
        TransferDto transferDto = TransferDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();
        when(accountServiceClient.transferToOtherAccount(transferDto))
                .thenReturn(Mono.empty());
        String expectedNotification = String.format("Перевод на другой счет на сумму %s %s выполнен. Конвертировано в %s %s",
                amount, fromCurrency, convertedAmount, toCurrency);
        NotificationRequestDto notificationRequestDto = NotificationRequestDto.builder().userId(fromAccountId).message(expectedNotification).build();
        when(notificationsServiceClient.sendNotification(eq(notificationRequestDto)))
                .thenReturn(Mono.empty());
        when(transferRepository.save(any(TransferDao.class))).thenReturn(Mono.just(new TransferDao()));
        when(transferMapper.transferResponseToTransferDao(
                any(TransferResponse.class),
                eq(fromCurrency),
                eq(toCurrency),
                any(LocalDateTime.class),
                eq(TransferType.EXTERNAL_TRANSFER),
                isNull()
        )).thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferToOtherAccount(request);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals(OperationStatus.SUCCESS) &&
                                response.getFromAccountId().equals(fromAccountId) &&
                                response.getToAccountId().equals(toAccountId) &&
                                response.getAmount().equals(amount) &&
                                response.getConvertedAmount().equals(convertedAmount)
                )
                .verifyComplete();

        verify(accountServiceClient).getAccountById(fromAccountId);
        verify(accountServiceClient).getAccountById(toAccountId);
        verify(blockerServiceClient).checkOperation(any(OperationCheckRequestDto.class));
        verify(exchangeServiceClient).convertCurrency(exchangeRequestDto);
        verify(accountServiceClient).transferToOtherAccount(transferDto);
        verify(notificationsServiceClient, times(1)).sendNotification(eq(notificationRequestDto));
        verify(transferRepository).save(any(TransferDao.class));
        verify(transferMapper).transferResponseToTransferDao(
                any(TransferResponse.class),
                eq(fromCurrency),
                eq(toCurrency),
                any(LocalDateTime.class),
                eq(TransferType.EXTERNAL_TRANSFER),
                isNull()
        );
    }

    @Test
    void transferToOtherAccount_shouldFailWhenSameAccount() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(fromAccountId)
                .amount(amount)
                .build();

        when(transferRepository.save(any())).thenReturn(Mono.empty());
        when(transferMapper.transferResponseToTransferDao(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferToOtherAccount(request);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void transferToOtherAccount_shouldFailWhenAmountZero() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(BigDecimal.ZERO)
                .build();

        when(transferRepository.save(any())).thenReturn(Mono.empty());
        when(transferMapper.transferResponseToTransferDao(any(), any(), any(), any(), any(), any()))
                .thenReturn(new TransferDao());

        Mono<TransferResponse> result = transferService.transferToOtherAccount(request);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }
}