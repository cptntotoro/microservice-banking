package ru.practicum.client.account;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.account.dto.AccountRequestDto;
import ru.practicum.client.account.dto.AccountResponseDto;
import ru.practicum.client.account.dto.SignUpResponseDto;
import ru.practicum.client.account.dto.UserFullResponseClientDto;
import ru.practicum.dto.auth.SignUpRequestDto;

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

    /**
     * Создание нового пользователя
     */
    public Mono<SignUpResponseDto> createUser(SignUpRequestDto signUpRequestDto) {
        String path = "/api/users/signup";
        String operation = "Creating account: " + signUpRequestDto;
        String errorPrefix = "Ошибка регистрации аккаунта: ";
        return performMono(HttpMethod.POST, path, signUpRequestDto, SignUpResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account created: {}", response));
    }

    /**
     * Получение счета по идентификатору
     */
    public Mono<AccountResponseDto> getAccount(UUID accountId) {
        String path = "/api/accounts/" + accountId;
        String operation = "Getting account by ID: " + accountId;
        String errorPrefix = "Ошибка получения счета: ";
        return performMono(HttpMethod.GET, path, null, AccountResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account retrieved: {}", accountId));
    }

    /**
     * Получение счета по идентификатору
     */
    public Mono<AccountResponseDto> getAccount(AccountRequestDto accountRequestDto) {
        String path = "/api/accounts/get";
        String operation = "Getting account by [userID, currencyCode]: {" + accountRequestDto.getUserId() + ", " + accountRequestDto.getCurrencyCode() + "}";
        String errorPrefix = "Ошибка получения счета: ";
        return performMono(HttpMethod.POST, path, accountRequestDto, AccountResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account retrieved: {}", response.getId()));
    }

//    /**
//     * Получение счета по идентификатору
//     */
//    public Mono<AccountResponseDto> getAccount(AccountByEmailRequestDto accountRequestDto) {
//        String path = "/api/accounts/get";
//        String operation = "Getting account by [userID, currencyCode]: {" + accountRequestDto.getUserId() + ", " + accountRequestDto.getCurrencyCode() + "}";
//        String errorPrefix = "Ошибка получения счета: ";
//        return performMono(HttpMethod.POST, path, accountRequestDto, AccountResponseDto.class, operation, errorPrefix, true)
//                .doOnSuccess(response -> log.info("Account retrieved: {}", response.getId()));
//    }

    /**
     * Получение счета по идентификатору
     */
    public Mono<AccountResponseDto> createAccount(AccountRequestDto accountRequestDto) {
        String path = "/api/accounts/create";
        String operation = "Create account: " + accountRequestDto;
        String errorPrefix = "Ошибка создания счета: ";
        return performMono(HttpMethod.POST, path, accountRequestDto, AccountResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account created: {}", response.getId()));
    }

    /**
     * Получение счета по идентификатору
     */
    public Mono<Void> deleteAccount(UUID accountId) {
        String path = "/api/accounts/" + accountId;
        String operation = "Delete account: " + accountId;
        String errorPrefix = "Ошибка удаления счета: ";
        return performMono(HttpMethod.DELETE, path, null, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account deleted: {}", accountId));
    }

    /**
     * Получение счетов пользователя
     */
    public Flux<AccountResponseDto> getUserAccounts(UUID userId) {
        String path = "/api/accounts/user/" + userId;
        String operation = "Getting accounts for user: " + userId;
        String errorPrefix = "Ошибка получения счетов пользователя: ";
        return performFlux(HttpMethod.GET, path, null, AccountResponseDto.class, operation, errorPrefix, true)
                .doOnNext(account -> log.debug("Retrieved account {} for user {}", account.getId(), userId));
    }

    /**
     * Получение полной информации о пользователе
     */
    public Mono<UserFullResponseClientDto> getFullUser(UUID userId) {
        String path = "/api/users/full/" + userId;
        String operation = "Getting full user profile by ID: " + userId;
        String errorPrefix = "Ошибка получения профиля пользователя: ";
        return performMono(HttpMethod.GET, path, null, UserFullResponseClientDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("User profile retrieved: {}", userId));
    }
}