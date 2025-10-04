package ru.practicum.service;

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

//    private final ReactiveUserDetailsService userDetailsService;
    private final AccountServiceClient accountServiceClient;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;

    /**
     * Аутентификация пользователя через Account Service и генерация JWT-токена
     * @param request DTO с логином и паролем
     * @return Mono с AuthResponse, содержащим токен и время жизни
     */
    public Mono<AuthResponse> authenticate(AuthRequest request) {
        return accountServiceClient.validateCredentials(request)
                .doOnNext(account -> log.info("Authenticating account00000000: {}", account))
                .flatMap(userDetails -> {
                    String token = jwtUtil.generateToken(userDetails);
                    log.info("Authenticating account11111111");
                    return Mono.just(new AuthResponse(token, jwtConfig.getExpiration()));
                })
                .doOnNext(account -> log.info("Authenticating account2222222: {}", account))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")));
    }

    /**
     * Валидация JWT-токена
     * @param token JWT-токен
     * @return Mono с результатом валидации (true/false)
     */
    public Mono<Boolean> validateToken(String token) {
        return Mono.just(jwtUtil.validateToken(token));
    }

//    /**
//     * Получение объекта Authentication из токена
//     * @param token JWT-токен
//     * @return Mono с Authentication, если токен валиден
//     */
//    public Mono<Authentication> getAuthentication(String token) {
//        if (jwtUtil.validateToken(token)) {
//            String username = jwtUtil.extractUsername(token);
//            return userDetailsService.findByUsername(username)
//                    .map(userDetails ->
//                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
//        }
//        return Mono.empty();
//    }

    /**
     * Извлечение имени пользователя из токена
     * @param token JWT-токен
     * @return Mono с именем пользователя, если токен валиден
     */
    public Mono<String> getUsernameFromToken(String token) {
        if (jwtUtil.validateToken(token)) {
            return Mono.just(jwtUtil.extractUsername(token));
        }
        return Mono.empty();
    }
    public Mono<String> getUserId(String token) {
        if (jwtUtil.validateToken(token)) {
            return Mono.just(jwtUtil.extractUserId(token));
        }
        return Mono.empty();
    }
}