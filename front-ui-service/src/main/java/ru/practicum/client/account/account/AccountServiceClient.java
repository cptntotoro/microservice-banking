package ru.practicum.client.account.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.user.LoginResponseClientDto;
import ru.practicum.dto.auth.LoginRequestDto;
import ru.practicum.dto.auth.SignUpRequestDto;
import ru.practicum.dto.auth.SignUpResponseDto;
import ru.practicum.exception.ServiceUnavailableException;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Клиент для обращений к сервису аккаунтов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<SignUpResponseDto> createAccount(SignUpRequestDto signUpRequestDto) {
        log.info("Creating account: {}", signUpRequestDto);

        return getAccountServiceUrl().flatMap(baseUrl -> {
                    String url = baseUrl + "/api/users/signup";
                    log.debug("Calling account service at: {}", url);

                    return webClientBuilder.build()
                            .post()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .bodyValue(signUpRequestDto)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("Account service error during signup: {}", response.statusCode());
                                return Mono.error(new ServiceUnavailableException(
                                        "Ошибка регистрации аккаунта: " + response.statusCode().value(),
                                        "account-service",
                                        "Сервис аккаунтов вернул ошибку: " + response.statusCode()));
                            })
                            .bodyToMono(SignUpResponseDto.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(response -> log.info("Account created: {}", response))
                            .doOnError(error -> log.error("Error creating account: {}", error.getMessage(), error));
                })
                .onErrorResume(e -> {
                    log.error("Failed to create account", e);
                    return Mono.error(new ServiceUnavailableException(
                            "Сервис аккаунтов временно недоступен: " + e.getMessage(),
                            "account-service",
                            "Проверьте подключение к сервису аккаунтов"));
                });
    }

    public Mono<AccountResponseClientDto> getAccount(UUID accountId) {
        log.info("Getting account by ID: {}", accountId);

        return getAccountServiceUrl().flatMap(baseUrl -> {
                    String url = baseUrl + "/api/accounts/" + accountId;
                    log.debug("Calling account service at: {}", url);

                    return webClientBuilder.build()
                            .get()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("Account service error getting account {}: {}", accountId, response.statusCode());
                                return Mono.error(new ServiceUnavailableException(
                                        "Ошибка получения счета: " + response.statusCode().value(),
                                        "account-service",
                                        "Сервис аккаунтов вернул ошибку: " + response.statusCode()));
                            })
                            .bodyToMono(AccountResponseClientDto.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(response -> log.info("Account retrieved: {}", accountId))
                            .doOnError(error -> log.error("Error getting account {}: {}", accountId, error.getMessage(), error));
                })
                .onErrorResume(e -> {
                    log.error("Failed to get account {}", accountId, e);
                    return Mono.error(new ServiceUnavailableException(
                            "Сервис аккаунтов временно недоступен: " + e.getMessage(),
                            "account-service",
                            "Проверьте подключение к сервису аккаунтов"));
                });
    }

    public Flux<AccountResponseClientDto> getUserAccounts(UUID userId) {
        log.info("Getting accounts for user: {}", userId);

        return getAccountServiceUrl().flatMapMany(baseUrl -> {
                    String url = baseUrl + "/api/accounts/user/" + userId;
                    log.debug("Calling account service at: {}", url);

                    return webClientBuilder.build()
                            .get()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("Account service error getting user accounts for {}: {}", userId, response.statusCode());
                                return Mono.error(new ServiceUnavailableException(
                                        "Ошибка получения счетов пользователя: " + response.statusCode().value(),
                                        "account-service",
                                        "Сервис аккаунтов вернул ошибку: " + response.statusCode()));
                            })
                            .bodyToFlux(AccountResponseClientDto.class)
                            .timeout(Duration.ofSeconds(10));
                })
                .doOnNext(account -> log.debug("Retrieved account {} for user {}", account.getId(), userId))
                .doOnComplete(() -> log.info("Successfully retrieved accounts for user {}", userId))
                .doOnError(error -> log.error("Error getting user accounts for {}: {}", userId, error.getMessage(), error))
                .onErrorResume(e -> {
                    log.error("Failed to get user accounts for {}", userId, e);
                    return Flux.error(new ServiceUnavailableException(
                            "Сервис аккаунтов временно недоступен: " + e.getMessage(),
                            "account-service",
                            "Проверьте подключение к сервису аккаунтов"));
                });
    }

    private Mono<String> getAccountServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("account-service");
            if (instances == null || instances.isEmpty()) {
                log.error("No instances of account-service found");
                throw new ServiceUnavailableException(
                        "Сервис аккаунтов не найден в реестре",
                        "account-service",
                        "Не удалось обнаружить экземпляры сервиса в Eureka/Consul");
            }

            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Discovered account-service at: {}", url);
            return url;
        }).onErrorResume(throwable -> {
            log.error("Error discovering account-service: {}", throwable.getMessage(), throwable);
            return Mono.error(new ServiceUnavailableException(
                    "Ошибка обнаружения сервиса аккаунтов",
                    "account-service",
                    "Проблема с реестром сервисов: " + throwable.getMessage()));
        });
    }

    public Mono<LoginResponseClientDto> login(LoginRequestDto loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getLogin());

        return getAccountServiceUrl().flatMap(baseUrl -> {
                    String url = baseUrl + "/api/auth/login";
                    log.debug("Calling account service login at: {}", url);

                    return webClientBuilder.build()
                            .post()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .bodyValue(loginRequest)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("Login failed with status: {}", response.statusCode());
                                return Mono.error(new ServiceUnavailableException(
                                        "Ошибка аутентификации: " + response.statusCode().value(),
                                        "account-service",
                                        "Сервис аккаунтов вернул ошибку при входе: " + response.statusCode()));
                            })
                            .bodyToMono(LoginResponseClientDto.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(response -> log.info("Login successful for user: {}", loginRequest.getLogin()))
                            .doOnError(error -> log.error("Error during login for user {}: {}", loginRequest.getLogin(), error.getMessage(), error));
                })
                .onErrorResume(e -> {
                    log.error("Failed to login", e);
                    return Mono.error(new ServiceUnavailableException(
                            "Сервис аккаунтов временно недоступен: " + e.getMessage(),
                            "account-service",
                            "Проверьте подключение к сервису аккаунтов"));
                });
    }
}