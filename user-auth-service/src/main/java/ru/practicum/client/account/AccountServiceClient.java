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
import ru.practicum.client.account.dto.UserResponseDto;
import ru.practicum.dto.AuthRequest;

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
     * Проверка учетных данных пользователя
     *
     * @param request DTO с логином и паролем
     * @return DTO пользователя
     */
    public Mono<UserResponseDto> validateCredentials(AuthRequest request) {
        String path = "/api/users/validate";
        String operation = "Validating credentials for user: " + request.getUsername();
        String errorPrefix = "Ошибка проверки учетных данных: ";
        return performMono(HttpMethod.POST, path, request, UserResponseDto.class, operation, errorPrefix, false)
                .doOnSuccess(response -> log.info("Credentials validated for user: {}", request.getUsername()));
    }
}