package ru.practicum.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.config.JwtConfig;
import ru.practicum.config.JwtUtil;
import ru.practicum.dto.AuthRequest;
import ru.practicum.dto.AuthResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    /**
     * Клиент для сервиса аккаунтов
     */
    private final AccountServiceClient accountServiceClient;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final MeterRegistry meterRegistry;

    private static final String LOGIN_SUCCESS_COUNT_TOTAL = "login_success_count_total";
    private static final String LOGIN_FAIL_COUNT_TOTAL = "login_fail_count_total";

    /**
     * Аутентификация пользователя через Account Service и генерация JWT-токена
     *
     * @param request DTO с логином и паролем
     * @return Mono с AuthResponse, содержащим токен и время жизни
     */
    public Mono<AuthResponse> authenticate(AuthRequest request) {
        return accountServiceClient.validateCredentials(request)
                .doOnNext(account -> log.info("Authenticating account: {}", account))
                .flatMap(userDetails -> {
                    String token = jwtUtil.generateToken(userDetails);
                    return Mono.just(new AuthResponse(token, jwtConfig.getExpiration()));
                })
                .doOnNext(account -> log.info("Authenticating account: {}", account))
                .doOnNext(account -> meterRegistry.counter(LOGIN_SUCCESS_COUNT_TOTAL, "login", request.getUsername()).increment())
                .switchIfEmpty(Mono.defer(() -> {
                    meterRegistry.counter(LOGIN_FAIL_COUNT_TOTAL, "login", request.getUsername()).increment();
                    return Mono.error(new RuntimeException("Invalid credentials"));
                }));
    }

    /**
     * Валидация JWT-токена
     *
     * @param token JWT-токен
     * @return Mono с результатом валидации (true/false)
     */
    public Mono<Boolean> validateToken(String token) {
        return Mono.just(jwtUtil.validateToken(token));
    }

    public Mono<String> getUserId(String token) {
        if (jwtUtil.validateToken(token)) {
            return Mono.just(jwtUtil.extractUserId(token));
        }
        return Mono.empty();
    }
}