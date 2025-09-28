package ru.practicum.client.account.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
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
import java.util.Random;
import java.util.UUID;

/**
 * Клиент для сервиса аккаунтов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;
    private final Random random = new Random();

    /**
     * Получение URL сервиса аккаунтов через DiscoveryClient
     */
    private Mono<String> getAccountServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("account-service");
            if (instances == null || instances.isEmpty()) {
                log.error("Экземпляры сервиса аккаунтов не найдены в реестре");
                throw new ServiceUnavailableException(
                        "Ошибка обнаружения сервиса: экземпляры не найдены",
                        "account-service",
                        "Проверьте регистрацию сервиса аккаунтов в Consul");
            }

            ServiceInstance instance = instances.get(random.nextInt(instances.size()));
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Вызов сервиса аккаунтов по адресу: {}", url);
            return url;
        }).onErrorResume(throwable -> {
            log.error("Ошибка при получении адреса сервиса аккаунтов: {}", throwable.getMessage());
            return Mono.error(new ServiceUnavailableException(
                    "Ошибка обнаружения сервиса: " + throwable.getMessage(),
                    "account-service",
                    "Проверьте работу реестра сервисов"));
        });
    }

    /**
     * Выполнение GET-запроса к сервису аккаунтов
     */
    private <T> Mono<T> executeGet(String path, Class<T> responseType, UUID id, String logMessageSuccess) {
        log.info("Начало {}: {}", logMessageSuccess, id);
        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + String.format(path, id);
            log.debug("Вызов сервиса аккаунтов по адресу: {}", url);
            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> handleError(response, "GET", url, logMessageSuccess, id))
                    .bodyToMono(responseType)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("Успешное завершение {}: {}", logMessageSuccess, id))
                    .doOnError(error -> log.error("Ошибка при {} {}: {}", logMessageSuccess, id, error.getMessage()));
        }).onErrorResume(throwable -> handleGeneralError(throwable, logMessageSuccess, id));
    }

    /**
     * Выполнение POST-запроса к сервису аккаунтов
     */
    private <T, B> Mono<T> executePost(String path, B body, Class<T> responseType, String logMessage, Object logParam) {
        log.info("Начало {}: {}", logMessage, logParam);
        return getAccountServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + path;
            log.debug("Вызов сервиса аккаунтов по адресу: {}", url);
            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> handleError(response, "POST", url, logMessage, logParam))
                    .bodyToMono(responseType)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("Успешное завершение {}: {}", logMessage, logParam))
                    .doOnError(error -> log.error("Ошибка при {} {}: {}", logMessage, logParam, error.getMessage()));
        }).onErrorResume(throwable -> handleGeneralError(throwable, logMessage, logParam));
    }

    /**
     * Обработка HTTP-ошибок от сервиса
     */
    private Mono<? extends Throwable> handleError(ClientResponse response, String method, String url, String operation, Object identifier) {
        log.error("Ошибка сервиса аккаунтов при {} {} по адресу {}: код состояния {}", operation, identifier, url, response.statusCode());
        return Mono.error(new ServiceUnavailableException(
                String.format("Ошибка %s %s: код состояния %d", operation, identifier, response.statusCode().value()),
                "account-service",
                "Проверьте доступность сервиса аккаунтов"));
    }

    /**
     * Обработка общих ошибок (например, таймаут или недоступность сервиса)
     */
    private <T> Mono<T> handleGeneralError(Throwable throwable, String operation, Object identifier) {
        log.error("Ошибка при {} {}: {}", operation, identifier, throwable.getMessage());
        return Mono.error(new ServiceUnavailableException(
                String.format("Ошибка %s %s: %s", operation, identifier, throwable.getMessage()),
                "account-service",
                "Проверьте подключение к сервису аккаунтов"));
    }

    /**
     * Создание нового пользователя
     */
    public Mono<SignUpResponseDto> createAccount(SignUpRequestDto signUpRequestDto) {
        return executePost("/api/users/signup", signUpRequestDto, SignUpResponseDto.class,
                "создания пользователя", signUpRequestDto.getUsername());
    }

    /**
     * Получение счета по идентификатору
     */
    public Mono<AccountResponseClientDto> getAccount(UUID accountId) {
        return executeGet("/api/accounts/%s", AccountResponseClientDto.class, accountId, "получения счета");
    }

    /**
     * Получение счетов пользователя
     */
    public Flux<AccountResponseClientDto> getUserAccounts(UUID userId) {
        log.info("Начало получения счетов пользователя: {}", userId);
        return getAccountServiceUrl().flatMapMany(baseUrl -> {
            String url = baseUrl + "/api/accounts/user/" + userId;
            log.debug("Вызов сервиса аккаунтов по адресу: {}", url);
            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> handleError(response, "GET", url, "получения счетов пользователя", userId))
                    .bodyToFlux(AccountResponseClientDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnNext(account -> log.debug("Получен счет {} для пользователя {}", account.getId(), userId))
                    .doOnComplete(() -> log.info("Успешное завершение получения счетов пользователя: {}", userId))
                    .doOnError(error -> log.error("Ошибка при получении счетов пользователя {}: {}", userId, error.getMessage()));
        }).onErrorResume(throwable -> Flux.error(new ServiceUnavailableException(
                String.format("Ошибка получения счетов пользователя %s: %s", userId, throwable.getMessage()),
                "account-service",
                "Проверьте подключение к сервису аккаунтов")));
    }

    /**
     * Аутентификация пользователя
     */
    public Mono<LoginResponseClientDto> login(LoginRequestDto loginRequest) {
        return executePost("/api/auth/login", loginRequest, LoginResponseClientDto.class,
                "аутентификации пользователя", loginRequest.getLogin());
    }

    /**
     * Получение полной информации о пользователе
     */
    public Mono<UserFullResponseClientDto> getFullUser(UUID userId) {
        return executeGet("/api/users/full/%s", UserFullResponseClientDto.class, userId,
                "получения полной информации о пользователе");
    }
}