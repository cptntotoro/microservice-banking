package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.dto.AuthRequest;

/**
 * Клиент для обращений к сервису аккаунтов
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
     * Проверка учетных данных пользователя через Account Service
     * @param request DTO с логином и паролем
     * @return Mono с UserResponseDto при успешной проверке
     */
    public Mono<UserResponseDto> validateCredentials(AuthRequest request) {
        String path = "/api/users/validate";
        String operation = "Validating credentials for user: " + request.getUsername();
        String errorPrefix = "Ошибка проверки учетных данных: ";
        return performMono(HttpMethod.POST, path, request, UserResponseDto.class, operation, errorPrefix, false)
                .doOnSuccess(response -> log.info("Credentials validated for user: {}", request.getUsername()));
    }
}