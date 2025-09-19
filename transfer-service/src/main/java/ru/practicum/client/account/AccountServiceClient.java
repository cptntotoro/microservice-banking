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

    public Mono<AccountResponseDto> getAccountByNumber(String accountNumber) {
        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/accounts/by-number/" + accountNumber;
            log.debug("Запрос на получение счета по номеру: {}", url);

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
                                            "getAccountByNumber",
                                            "Ошибка при получении счета: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(AccountResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "getAccountByNumber", "Таймаут при получении счета"))
                    .doOnSuccess(account -> log.info("Счет с номером {} успешно получен", accountNumber))
                    .doOnError(error -> log.error("Ошибка при получении счета с номером {}: {}", accountNumber, error.getMessage()));
        });
    }

    public Mono<AccountResponseDto> deposit(UUID accountId, BigDecimal amount) {
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
                                            "Ошибка при пополнении счета: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(AccountResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "deposit", "Таймаут при пополнении счета"))
                    .doOnSuccess(account -> log.info("Счет {} успешно пополнен на сумму {}", accountId, amount))
                    .doOnError(error -> log.error("Ошибка при пополнении счета {} на сумму {}: {}", accountId, amount, error.getMessage()));
        });
    }

    public Mono<AccountResponseDto> withdraw(UUID accountId, BigDecimal amount) {
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
                                            "Ошибка при снятии со счета: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(AccountResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("account-service", "withdraw", "Таймаут при снятии со счета"))
                    .doOnSuccess(account -> log.info("Снято {} со счета {}", amount, accountId))
                    .doOnError(error -> log.error("Ошибка при снятии {} со счета {}: {}", amount, accountId, error.getMessage()));
        });
    }

    public Mono<Void> transferBetweenOwnAccounts(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        TransferDto dto = TransferDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
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

    public Mono<Void> transferToOtherAccount(UUID fromAccountId, String toAccountNumber, BigDecimal amount) {
        TransferDto dto = TransferDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountNumber(toAccountNumber)
                .amount(amount)
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
                    .doOnSuccess(v -> log.info("Перевод {} со счета {} на счет {} выполнен", amount, fromAccountId, toAccountNumber))
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