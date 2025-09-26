package ru.practicum.client.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.exception.ServiceClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<AccountResponseDto> getAccountById(UUID accountId) {
        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/accounts/" + accountId;
            log.debug("Запрос на получение счета по ID: {}", url);

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "account-service",
                                            "getAccountById",
                                            "Ошибка при получении счета: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(AccountResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "getAccountById", "Таймаут при получении счета"))
                    .doOnSuccess(account -> log.info("Счет с ID {} успешно получен", accountId))
                    .doOnError(error -> log.error("Ошибка при получении счета с ID {}: {}", accountId, error.getMessage()));
        });
    }

    public Mono<AccountResponseDto> getAccountByUserUsername(String username) {
        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/accounts/by-username/" + username;
            log.debug("Запрос на получение счета по username: {}", url);

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "account-service",
                                            "getAccountByUserUsername",
                                            "Ошибка при получении счета: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(AccountResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "getAccountByUserUsername", "Таймаут при получении счета"))
                    .doOnSuccess(account -> log.info("Счет пользователя с username {} успешно получен", username))
                    .doOnError(error -> log.error("Ошибка при получении счета пользователя с username {}: {}", username, error.getMessage()));
        });
    }

    public Mono<Void> deposit(UUID accountId, BigDecimal amount) {
        DepositWithdrawDto dto = DepositWithdrawDto.builder()
                .accountId(accountId)
                .amount(amount)
                .build();

        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/accounts/deposit";
            log.debug("Запрос на пополнение счета: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "account-service",
                                            "deposit",
                                            "Ошибка при пополнении: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "deposit", "Таймаут при пополнении"))
                    .doOnSuccess(v -> log.info("Пополнение {} на счет {} выполнено", amount, accountId))
                    .doOnError(error -> log.error("Ошибка при пополнении счета: {}", error.getMessage()));
        });
    }

    public Mono<Void> withdraw(UUID accountId, BigDecimal amount) {
        DepositWithdrawDto dto = DepositWithdrawDto.builder()
                .accountId(accountId)
                .amount(amount)
                .build();

        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/accounts/withdraw";
            log.debug("Запрос на снятие со счета: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "account-service",
                                            "withdraw",
                                            "Ошибка при снятии: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "withdraw", "Таймаут при снятии"))
                    .doOnSuccess(v -> log.info("Снятие {} со счета {} выполнено", amount, accountId))
                    .doOnError(error -> log.error("Ошибка при снятии со счета: {}", error.getMessage()));
        });
    }

    public Mono<Void> transferBetweenOwnAccounts(UUID fromAccountId, UUID toAccountId, BigDecimal amount, BigDecimal convertedAmount) {
        TransferDto dto = TransferDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .amount(convertedAmount)
                .build();

        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/accounts/transfer/own";
            log.debug("Запрос на перевод между своими счетами: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "account-service",
                                            "transferBetweenOwnAccounts",
                                            "Ошибка при переводе: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "transferBetweenOwnAccounts", "Таймаут при переводе"))
                    .doOnSuccess(v -> log.info("Перевод {} между счетами {} и {} выполнен", amount, fromAccountId, toAccountId))
                    .doOnError(error -> log.error("Ошибка при переводе между счетами: {}", error.getMessage()));
        });
    }

    public Mono<Void> transferToOtherAccount(UUID fromAccountId, UUID toAccountId, BigDecimal amount, BigDecimal convertedAmount) {
        TransferDto dto = TransferDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .amount(convertedAmount)
                .build();

        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/accounts/transfer/other";
            log.debug("Запрос на перевод на другой счет: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "account-service",
                                            "transferToOtherAccount",
                                            "Ошибка при переводе: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "transferToOtherAccount", "Таймаут при переводе"))
                    .doOnSuccess(v -> log.info("Перевод {} со счета {} на счет {} выполнен", amount, fromAccountId, toAccountId))
                    .doOnError(error -> log.error("Ошибка при переводе на другой счет: {}", error.getMessage()));
        });
    }

    private Mono<String> getAccountServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("account-service");
            if (instances == null || instances.isEmpty()) {
                throw ServiceClientException.unavailable("account-service", "discovery", "Сервис аккаунтов недоступен");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Обнаружен account-service по адресу: {}", url);
            return url;
        }).onErrorResume(e -> {
            log.error("Не удалось подключиться к account-service: {}", e.getMessage());
            return Mono.error(ServiceClientException.unavailable("account-service", "discovery", e.getMessage()));
        });
    }
}