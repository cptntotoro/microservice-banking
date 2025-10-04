package ru.practicum.client.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.account.dto.BalanceUpdateRequestDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Клиент для обращений к сервису аккаунтов
 */
@Component
@Slf4j
public class AccountsServiceClient extends BaseServiceClient {

    @Autowired
    public AccountsServiceClient(@Qualifier("accountServiceWebClient") WebClient webClient, DiscoveryClient discoveryClient) {
        super(webClient, discoveryClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "accounts-service";
    }

    public Mono<Boolean> verifyAccount(UUID accountId, UUID userId) {
        String path = "/accounts/verify?accountId=" + accountId + "&userId=" + userId;
        String operation = "Verify account: " + accountId;
        String errorPrefix = "Ошибка верификации счета: ";
        return performMono(HttpMethod.GET, path, null, Boolean.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account verified: {}", accountId));
    }

    public Mono<BigDecimal> getAccountBalance(UUID accountId) {
        String path = "/accounts/balance?accountId=" + accountId;
        String operation = "Get account balance: " + accountId;
        String errorPrefix = "Ошибка верификации счета: ";
        return performMono(HttpMethod.GET, path, null, BigDecimal.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account balance got: {}", accountId));
    }

    public Mono<Void> updateAccountBalance(BalanceUpdateRequestDto request) {
        String path = "/accounts/update-balance";
        String operation = "Update account balance: " + request;
        String errorPrefix = "Ошибка обновления баланса счета: ";
        return performMono(HttpMethod.POST, path, request, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account balance updated"));
    }
}