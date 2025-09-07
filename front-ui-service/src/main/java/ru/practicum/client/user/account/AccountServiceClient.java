package ru.practicum.client.user.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.user.auth.LoginResponseDto;
import ru.practicum.dto.auth.LoginRequestDto;
import ru.practicum.dto.auth.SignUpRequestDto;
import ru.practicum.dto.auth.SignUpResponseDto;

import java.time.Duration;
import java.util.List;

/**
 * Клиент обращения к сервису аккаунтов
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

            return webClientBuilder.build().post().uri(url).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).bodyValue(signUpRequestDto).retrieve().bodyToMono(SignUpResponseDto.class).timeout(Duration.ofSeconds(10)).doOnSuccess(response -> log.info("Account created: {}", response)).doOnError(error -> log.error("Error creating account: ", error));
        }).onErrorResume(e -> {
            log.error("Failed to create account", e);
            return Mono.error(new RuntimeException("Account service unavailable: " + e.getMessage()));
        });
    }

    private Mono<String> getAccountServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("account-service");
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("No instances of account-service found");
            }

            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Discovered account-service at: {}", url);
            return url;
        });
    }

    public Mono<LoginResponseDto> login(LoginRequestDto loginRequest) {
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
                    .onStatus(status -> status.isError(), response -> {
                        log.error("Login failed with status: {}", response.statusCode());
                        return Mono.error(new RuntimeException("Authentication failed"));
                    })
                    .bodyToMono(LoginResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("Login successful for user: {}", loginRequest.getLogin()))
                    .doOnError(error -> log.error("Error during login: ", error));
        }).onErrorResume(e -> {
            log.error("Failed to login", e);
            return Mono.error(new RuntimeException("Account service unavailable: " + e.getMessage()));
        });
    }



//    public Mono<SignUpResponseDto> getAccount(Long id, String token) {
//        return webClientBuilder.build()
//                .get()
//                .uri(ACCOUNT_SERVICE_URL + "/api/accounts/{id}", id)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .retrieve()
//                .bodyToMono(SignUpResponseDto.class)
//                .onErrorResume(e -> {
//                    System.err.println("Error fetching account: " + e.getMessage());
//                    return Mono.error(new RuntimeException("Failed to get account"));
//                });
//    }
//
//    public Mono<SignUpResponseDto> updateAccount(Long id, SignUpRequestDto request, String token) {
//        return webClientBuilder.build()
//                .put()
//                .uri(ACCOUNT_SERVICE_URL + "/api/accounts/{id}", id)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(SignUpResponseDto.class)
//                .onErrorResume(e -> {
//                    System.err.println("Error updating account: " + e.getMessage());
//                    return Mono.error(new RuntimeException("Failed to update account"));
//                });
//    }
//
//    public Mono<Void> deleteAccount(Long id, String token) {
//        return webClientBuilder.build()
//                .delete()
//                .uri(ACCOUNT_SERVICE_URL + "/api/accounts/{id}", id)
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .retrieve()
//                .bodyToMono(Void.class)
//                .onErrorResume(e -> {
//                    System.err.println("Error deleting account: " + e.getMessage());
//                    return Mono.error(new RuntimeException("Failed to delete account"));
//                });
//    }
}
