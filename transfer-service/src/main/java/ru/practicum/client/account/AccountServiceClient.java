package ru.practicum.client.account;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.account.dto.AccountResponseDto;
import ru.practicum.client.account.dto.TransferDto;

import java.util.UUID;

/**
 * Клиент для сервиса аккаунтов
 */
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

    public Mono<AccountResponseDto> getAccountWithUserByAccountId(UUID accountId) {
        String path = "/api/accounts/user/" + accountId;
        String operation = "Getting account by ID: " + accountId;
        String errorPrefix = "Ошибка получения счета: ";
        return performMono(HttpMethod.GET, path, null, AccountResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account retrieved: {}", response));
    }

    public Mono<AccountResponseDto> getAccountWithUserByEmailAndCurrency(String email, String currency) {
        String path = "/api/accounts/user-by-email/" + email + "/" + currency;
        String operation = "Getting account by email: " + email;
        String errorPrefix = "Ошибка получения счета: ";
        return performMono(HttpMethod.GET, path, null, AccountResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account retrieved: {}", response));
    }

    public Mono<Void> transfer(TransferDto transferDto) {
        String path = "/api/accounts/transfer";
        String operation = "transferToOtherAccount: " + transferDto;
        String errorPrefix = "Ошибка перевода средств: ";
        return performMono(HttpMethod.POST, path, transferDto, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("transferToOtherAccount success"));
    }
}