package ru.practicum.client.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Клиент обращения к сервису аккаунтов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountsServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<Boolean> verifyAccount(UUID accountId, UUID userId) {
        log.info("Verifying account {} for user {}", accountId, userId);

        return getAccountsServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/accounts/verify?accountId=" + accountId + "&userId=" + userId;
            log.debug("Calling Accounts service at: {}", url);

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .doOnSuccess(response -> log.info("Successfully verified account"))
                    .doOnError(error -> log.error("Error verifying account: {}", error.getMessage()));
        }).onErrorResume(e -> {
            log.error("Failed to verify account", e);
            return Mono.just(false);
        });
    }

    public Mono<BigDecimal> getAccountBalance(UUID accountId) {
        log.info("Fetching balance for account {}", accountId);

        return getAccountsServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/accounts/balance?accountId=" + accountId;
            log.debug("Calling Accounts service at: {}", url);

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(BigDecimal.class)
                    .doOnSuccess(response -> log.info("Successfully fetched balance"))
                    .doOnError(error -> log.error("Error fetching balance: {}", error.getMessage()));
        }).onErrorResume(e -> {
            log.error("Failed to fetch balance", e);
            return Mono.error(new RuntimeException("Accounts service unavailable: " + e.getMessage()));
        });
    }

    public Mono<Void> updateAccountBalance(BalanceUpdateRequestDto request) {
        log.info("Updating balance for account {}", request.getAccountId());

        return getAccountsServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/accounts/update-balance";
            log.debug("Calling Accounts service at: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.info("Successfully updated balance"))
                    .doOnError(error -> log.error("Error updating balance: {}", error.getMessage()));
        }).onErrorResume(e -> {
            log.error("Failed to update balance", e);
            return Mono.error(new RuntimeException("Accounts service unavailable: " + e.getMessage()));
        });
    }

    private Mono<String> getAccountsServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("accounts-service");
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("No instances of accounts-service found");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Discovered accounts-service at: {}", url);
            return url;
        });
    }
}