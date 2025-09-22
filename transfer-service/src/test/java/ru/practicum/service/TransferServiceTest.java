package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.client.account.AccountResponseDto;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.client.blocker.BlockerServiceClient;
import ru.practicum.client.exchange.ConvertResponseDto;
import ru.practicum.client.exchange.ExchangeServiceClient;
import ru.practicum.client.notification.NotificationsServiceClient;
import ru.practicum.exception.ServiceUnavailableException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private TransferServiceImpl transferService;

    private UUID fromAccountId;
    private UUID toAccountId;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal convertedAmount;

    @BeforeEach
    void setUp() {
        fromAccountId = UUID.randomUUID();
        toAccountId = UUID.randomUUID();
        fromCurrency = "USD";
        toCurrency = "EUR";
        amount = new BigDecimal("100.00");
        convertedAmount = new BigDecimal("85.00");
    }

    private AccountResponseDto createAccountResponse(UUID id, String currencyCode, BigDecimal balance) {
        return AccountResponseDto.builder()
                .accountId(id)
                .currencyCode(currencyCode)
                .balance(balance)
                .accountNumber("ACC" + id.toString().substring(0, 8))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ConvertResponseDto createConvertResponse() {
        return ConvertResponseDto.builder()
                .originalAmount(amount)
                .fromCurrency(fromCurrency)
                .convertedAmount(convertedAmount)
                .toCurrency(toCurrency)
                .exchangeRate(new BigDecimal("0.85"))
                .convertedAt(LocalDateTime.now())
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
        ConvertResponseDto convertResponse = createConvertResponse();

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountById(toAccountId)).thenReturn(Mono.just(toAccount));
        when(exchangeServiceClient.convertCurrency(fromCurrency, toCurrency, amount))
                .thenReturn(Mono.just(convertResponse));
        when(blockerServiceClient.checkSuspicious(fromAccountId, toAccountId, amount, true))
                .thenReturn(Mono.just(false));
        when(accountServiceClient.transferBetweenOwnAccounts(fromAccountId, toAccountId, amount))
                .thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any(), any())).thenReturn(Mono.empty());

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("SUCCESS") &&
                                response.getFromAccountId().equals(fromAccountId) &&
                                response.getToAccountId().equals(toAccountId) &&
                                response.getAmount().equals(amount) &&
                                response.getConvertedAmount().equals(convertedAmount)
                )
                .verifyComplete();

        verify(accountServiceClient).getAccountById(fromAccountId);
        verify(accountServiceClient).getAccountById(toAccountId);
        verify(exchangeServiceClient).convertCurrency(fromCurrency, toCurrency, amount);
        verify(blockerServiceClient).checkSuspicious(fromAccountId, toAccountId, amount, true);
        verify(accountServiceClient).transferBetweenOwnAccounts(fromAccountId, toAccountId, amount);
        verify(notificationsServiceClient).sendNotification(any(), any());
    }

    @Test
    void transferBetweenOwnAccounts_shouldFailWhenAmountZero() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(BigDecimal.ZERO)
                .build();

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
        ConvertResponseDto convertResponse = createConvertResponse();

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountById(toAccountId)).thenReturn(Mono.just(toAccount));
        when(exchangeServiceClient.convertCurrency(fromCurrency, toCurrency, amount))
                .thenReturn(Mono.just(convertResponse));
        when(blockerServiceClient.checkSuspicious(fromAccountId, toAccountId, amount, true))
                .thenReturn(Mono.just(true));

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

        when(accountServiceClient.getAccountById(fromAccountId))
                .thenReturn(Mono.error(exception));
        when(accountServiceClient.getAccountById(toAccountId))
                .thenReturn(Mono.just(createAccountResponse(toAccountId, toCurrency, new BigDecimal("50.00"))));

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof ServiceUnavailableException &&
                                error.getMessage().contains("account-service") &&
                                error.getMessage().contains("Не удалось получить счет отправителя")
                )
                .verify();

        verify(accountServiceClient).getAccountById(fromAccountId);
        verifyNoInteractions(exchangeServiceClient, blockerServiceClient, notificationsServiceClient);
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
        ConvertResponseDto convertResponse = createConvertResponse();

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountById(toAccountId)).thenReturn(Mono.just(toAccount));
        when(exchangeServiceClient.convertCurrency(fromCurrency, toCurrency, amount))
                .thenReturn(Mono.just(convertResponse));
        when(blockerServiceClient.checkSuspicious(fromAccountId, toAccountId, amount, true))
                .thenReturn(Mono.just(false));
        when(accountServiceClient.transferBetweenOwnAccounts(fromAccountId, toAccountId, amount))
                .thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Notification failed")));

        Mono<TransferResponse> result = transferService.transferBetweenOwnAccounts(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatus().equals("SUCCESS"))
                .verifyComplete();

        verify(notificationsServiceClient).sendNotification(any(), any());
    }

    @Test
    void transferToOtherAccount_shouldSuccess() {
        String toAccountNumber = "40702810500000012345";
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountNumber(toAccountNumber)
                .amount(amount)
                .build();

        AccountResponseDto fromAccount = createAccountResponse(fromAccountId, fromCurrency, new BigDecimal("200.00"));
        AccountResponseDto toAccount = createAccountResponse(toAccountId, toCurrency, new BigDecimal("50.00"));
        ConvertResponseDto convertResponse = createConvertResponse();

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountByNumber(toAccountNumber)).thenReturn(Mono.just(toAccount));
        when(exchangeServiceClient.convertCurrency(fromCurrency, toCurrency, amount))
                .thenReturn(Mono.just(convertResponse));
        when(blockerServiceClient.checkSuspicious(fromAccountId, toAccountId, amount, false))
                .thenReturn(Mono.just(false));
        when(accountServiceClient.transferToOtherAccount(fromAccountId, toAccountNumber, amount))
                .thenReturn(Mono.empty());
        when(notificationsServiceClient.sendNotification(any(), any())).thenReturn(Mono.empty());

        Mono<TransferResponse> result = transferService.transferToOtherAccount(request);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatus().equals("SUCCESS") &&
                                response.getFromAccountId().equals(fromAccountId) &&
                                response.getToAccountId().equals(toAccountId) &&
                                response.getAmount().equals(amount) &&
                                response.getConvertedAmount().equals(convertedAmount)
                )
                .verifyComplete();

        verify(accountServiceClient).getAccountById(fromAccountId);
        verify(accountServiceClient).getAccountByNumber(toAccountNumber);
        verify(exchangeServiceClient).convertCurrency(fromCurrency, toCurrency, amount);
        verify(blockerServiceClient).checkSuspicious(fromAccountId, toAccountId, amount, false);
        verify(accountServiceClient).transferToOtherAccount(fromAccountId, toAccountNumber, amount);
        verify(notificationsServiceClient, times(2)).sendNotification(any(), any());
    }

    @Test
    void transferToOtherAccount_shouldFailWhenSameAccount() {
        String toAccountNumber = "40702810500000012345";
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountNumber(toAccountNumber)
                .amount(amount)
                .build();

        AccountResponseDto fromAccount = createAccountResponse(fromAccountId, fromCurrency, new BigDecimal("200.00"));
        AccountResponseDto toAccount = createAccountResponse(fromAccountId, toCurrency, new BigDecimal("50.00"));

        when(accountServiceClient.getAccountById(fromAccountId)).thenReturn(Mono.just(fromAccount));
        when(accountServiceClient.getAccountByNumber(toAccountNumber)).thenReturn(Mono.just(toAccount));

        Mono<TransferResponse> result = transferService.transferToOtherAccount(request);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void transferToOtherAccount_shouldFailWhenAmountZero() {
        String toAccountNumber = "40702810500000012345";
        TransferRequest request = TransferRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountNumber(toAccountNumber)
                .amount(BigDecimal.ZERO)
                .build();

        Mono<TransferResponse> result = transferService.transferToOtherAccount(request);

        StepVerifier.create(result)
                .expectError(ValidationException.class)
                .verify();
    }
}