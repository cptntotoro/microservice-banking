package ru.practicum.client.account;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.account.dto.AccountResponseDto;
import ru.practicum.client.account.dto.DepositWithdrawDto;
import ru.practicum.client.account.dto.TransferDto;
import ru.practicum.exception.ServiceClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class AccountServiceClient extends BaseServiceClient {
    @Autowired
    public AccountServiceClient(@Qualifier("accountServiceWebClient") WebClient webClient, DiscoveryClient discoveryClient) {
        super(webClient, discoveryClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "account-service";
    }

    public Mono<AccountResponseDto> getAccountById(UUID accountId) {
        String path = "/api/accounts/" + accountId;
        String operation = "Getting account by ID: " + accountId;
        String errorPrefix = "Ошибка получения счета: ";
        return performMono(HttpMethod.GET, path, null, AccountResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account retrieved: {}", accountId));
    }

    public Mono<AccountResponseDto> getAccountByUserUsername(String username) {
        String path = "/api/accounts/by-username/" + username;
        String operation = "Getting account by username: " + username;
        String errorPrefix = "Ошибка получения счета: ";
        return performMono(HttpMethod.GET, path, null, AccountResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account retrieved: {}", response.getId()));
    }

    public Mono<Void> deposit(DepositWithdrawDto depositWithdrawDto) {
        String path = "/api/accounts/deposit";
        String operation = "Deposit: " + depositWithdrawDto;
        String errorPrefix = "Ошибка депозита: ";
        return performMono(HttpMethod.POST, path, depositWithdrawDto, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Deposit success"));
    }

    public Mono<Void> withdraw(DepositWithdrawDto depositWithdrawDto) {
        String path = "/api/accounts/withdraw";
        String operation = "Withdraw: " + depositWithdrawDto;
        String errorPrefix = "Ошибка вывода средств: ";
        return performMono(HttpMethod.POST, path, depositWithdrawDto, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Withdraw success"));
    }

    public Mono<Void> transferBetweenOwnAccounts(TransferDto transferDto) {
        String path = "/api/accounts/transfer/own";
        String operation = "transferBetweenOwnAccounts: " + transferDto;
        String errorPrefix = "Ошибка перевода средств: ";
        return performMono(HttpMethod.POST, path, transferDto, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("transferBetweenOwnAccounts success"));
    }

    public Mono<Void> transferToOtherAccount(TransferDto transferDto) {
        String path = "/api/accounts/transfer/other";
        String operation = "transferToOtherAccount: " + transferDto;
        String errorPrefix = "Ошибка перевода средств: ";
        return performMono(HttpMethod.POST, path, transferDto, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("transferToOtherAccount success"));
    }
}