package ru.practicum.client.auth;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.auth.dto.LoginResponseClientDto;
import ru.practicum.client.auth.dto.TokenResponseDto;
import ru.practicum.client.auth.dto.TokenValidationRequestDto;
import ru.practicum.client.auth.dto.UserProfileResponseClientDto;
import ru.practicum.dto.auth.ChangePasswordRequestDto;
import ru.practicum.dto.auth.LoginRequestDto;
import ru.practicum.dto.user.EditUserProfileDto;

/**
 * Клиент сервиса аутентификации
 */
@Component
@Slf4j
public class AuthServiceClient extends BaseServiceClient {

    @Autowired
    public AuthServiceClient(@Qualifier("userAuthServiceWebClient") WebClient webClient) {
        super(webClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "user-auth-service";
    }

    public Mono<TokenResponseDto> login(LoginRequestDto loginRequest) {
        String path = "/auth/login";
        String operation = "Login: " + loginRequest;
        String errorPrefix = "Ошибка логина аккаунта: ";
        return performMono(HttpMethod.POST, path, loginRequest, TokenResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account created: {}", response));
    }

    public Mono<String> getUserId(String token) {
        String path = "/auth/getUserId";
        String operation = "Token: " + token;
        String errorPrefix = "Ошибка получения userId: ";
        return performMono(HttpMethod.POST, path, new TokenValidationRequestDto(token), String.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Account created: {}", response));
    }

    public Mono<LoginResponseClientDto> refreshToken(String refreshToken) {
        String path = "/auth/refresh";
        String operation = "Refreshing token: " + refreshToken;
        String errorPrefix = "Ошибка обновления токена: ";
        return performMono(HttpMethod.POST, path, null, LoginResponseClientDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Token refreshed : {}", response));
    }

    public Mono<UserProfileResponseClientDto> getProfile(String token) {
        return Mono.just(new UserProfileResponseClientDto());
//        return webClientBuilder.build()
//                .get()
//                .uri(AUTH_SERVICE_URL + "/auth/profile")
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .retrieve()
//                .bodyToMono(UserProfileResponseClientDto.class)
//                .onErrorResume(e -> {
//                    System.err.println("Error getting profile: " + e.getMessage());
//                    return Mono.error(new RuntimeException("Failed to get profile"));
//                });
    }

    public Mono<UserProfileResponseClientDto> updateProfile(EditUserProfileDto updateRequest, String token) {
        return Mono.just(new UserProfileResponseClientDto());
//        return webClientBuilder.build()
//                .put()
//                .uri(AUTH_SERVICE_URL + "/auth/profile")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .bodyValue(updateRequest)
//                .retrieve()
//                .bodyToMono(UserProfileResponseClientDto.class)
//                .onErrorResume(e -> {
//                    System.err.println("Error updating profile: " + e.getMessage());
//                    return Mono.error(new RuntimeException("Failed to update profile"));
//                });
    }

    public Mono<ResponseEntity<Void>> changePassword(ChangePasswordRequestDto changePasswordRequest, String token) {
        return Mono.empty();
//        return webClientBuilder.build()
//                .post()
//                .uri(AUTH_SERVICE_URL + "/auth/change-password")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .bodyValue(changePasswordRequest)
//                .retrieve()
//                .toBodilessEntity()
//                .onErrorResume(e -> {
//                    System.err.println("Error changing password: " + e.getMessage());
//                    return Mono.error(new RuntimeException("Failed to change password"));
//                });
    }

    public Mono<ResponseEntity<Void>> deleteAccount(String token) {
        return Mono.empty();
//        return webClientBuilder.build()
//                .delete()
//                .uri(AUTH_SERVICE_URL + "/auth/account")
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .retrieve()
//                .toBodilessEntity()
//                .onErrorResume(e -> {
//                    System.err.println("Error deleting account: " + e.getMessage());
//                    return Mono.error(new RuntimeException("Failed to delete account"));
//                });
    }

    public Mono<ResponseEntity<Void>> validateToken(String token) {
        return Mono.empty();
//        return webClientBuilder.build()
//                .get()
//                .uri(AUTH_SERVICE_URL + "/auth/validate")
//                .header(HttpHeaders.AUTHORIZATION, token)
//                .retrieve()
//                .toBodilessEntity()
//                .onErrorResume(e -> {
//                    System.err.println("Error validating token: " + e.getMessage());
//                    return Mono.error(new RuntimeException("Token validation failed"));
//                });
    }

    public Mono<Boolean> checkUsernameAvailability(String username) {
        return Mono.just(false);
//        return webClientBuilder.build()
//                .get()
//                .uri(AUTH_SERVICE_URL + "/auth/check-username?username={username}", username)
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .onErrorResume(e -> {
//                    System.err.println("Error checking username availability: " + e.getMessage());
//                    return Mono.just(false);
//                });
    }

    public Mono<Boolean> checkEmailAvailability(String email) {
        return Mono.just(false);
//        return webClientBuilder.build()
//                .get()
//                .uri(AUTH_SERVICE_URL + "/auth/check-email?email={email}", email)
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .onErrorResume(e -> {
//                    System.err.println("Error checking email availability: " + e.getMessage());
//                    return Mono.just(false);
//                });
    }
}
