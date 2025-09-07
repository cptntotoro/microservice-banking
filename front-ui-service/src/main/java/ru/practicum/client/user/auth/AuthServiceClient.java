package ru.practicum.client.user.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.dto.auth.*;

/**
 * Клиент обращения к сервису аутентификации
 */
@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;

    private static final String AUTH_SERVICE_URL = "http://auth-service";

    public Mono<LoginResponseDto> login(LoginRequestDto loginRequest) {
        return webClientBuilder.build()
                .post()
                .uri(AUTH_SERVICE_URL + "/auth/login")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponseDto.class)
                .onErrorResume(e -> {
                    System.err.println("Error during login: " + e.getMessage());
                    return Mono.error(new RuntimeException("Login failed"));
                });
    }

    public Mono<SignUpResponseDto> signup(SignUpRequestDto signupRequest) {
        return webClientBuilder.build()
                .post()
                .uri(AUTH_SERVICE_URL + "/auth/signup")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(signupRequest)
                .retrieve()
                .bodyToMono(SignUpResponseDto.class)
                .onErrorResume(e -> {
                    System.err.println("Error during signup: " + e.getMessage());
                    return Mono.error(new RuntimeException("Signup failed"));
                });
    }

    public Mono<ResponseEntity<Void>> logout(String token) {
        return webClientBuilder.build()
                .post()
                .uri(AUTH_SERVICE_URL + "/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> {
                    System.err.println("Error during logout: " + e.getMessage());
                    return Mono.error(new RuntimeException("Logout failed"));
                });
    }

    public Mono<LoginResponseDto> refreshToken(String refreshToken) {
        return webClientBuilder.build()
                .post()
                .uri(AUTH_SERVICE_URL + "/auth/refresh")
                .header(HttpHeaders.AUTHORIZATION, refreshToken)
                .retrieve()
                .bodyToMono(LoginResponseDto.class)
                .onErrorResume(e -> {
                    System.err.println("Error refreshing token: " + e.getMessage());
                    return Mono.error(new RuntimeException("Token refresh failed"));
                });
    }

    public Mono<UserProfileResponseDto> getProfile(String token) {
        return webClientBuilder.build()
                .get()
                .uri(AUTH_SERVICE_URL + "/auth/profile")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(UserProfileResponseDto.class)
                .onErrorResume(e -> {
                    System.err.println("Error getting profile: " + e.getMessage());
                    return Mono.error(new RuntimeException("Failed to get profile"));
                });
    }

    public Mono<UserProfileResponseDto> updateProfile(UpdateProfileRequestDto updateRequest, String token) {
        return webClientBuilder.build()
                .put()
                .uri(AUTH_SERVICE_URL + "/auth/profile")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(updateRequest)
                .retrieve()
                .bodyToMono(UserProfileResponseDto.class)
                .onErrorResume(e -> {
                    System.err.println("Error updating profile: " + e.getMessage());
                    return Mono.error(new RuntimeException("Failed to update profile"));
                });
    }

    public Mono<ResponseEntity<Void>> changePassword(ChangePasswordRequestDto changePasswordRequest, String token) {
        return webClientBuilder.build()
                .post()
                .uri(AUTH_SERVICE_URL + "/auth/change-password")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(changePasswordRequest)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> {
                    System.err.println("Error changing password: " + e.getMessage());
                    return Mono.error(new RuntimeException("Failed to change password"));
                });
    }

    public Mono<ResponseEntity<Void>> deleteAccount(String token) {
        return webClientBuilder.build()
                .delete()
                .uri(AUTH_SERVICE_URL + "/auth/account")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> {
                    System.err.println("Error deleting account: " + e.getMessage());
                    return Mono.error(new RuntimeException("Failed to delete account"));
                });
    }

    public Mono<ResponseEntity<Void>> validateToken(String token) {
        return webClientBuilder.build()
                .get()
                .uri(AUTH_SERVICE_URL + "/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> {
                    System.err.println("Error validating token: " + e.getMessage());
                    return Mono.error(new RuntimeException("Token validation failed"));
                });
    }

    public Mono<Boolean> checkUsernameAvailability(String username) {
        return webClientBuilder.build()
                .get()
                .uri(AUTH_SERVICE_URL + "/auth/check-username?username={username}", username)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(e -> {
                    System.err.println("Error checking username availability: " + e.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Boolean> checkEmailAvailability(String email) {
        return webClientBuilder.build()
                .get()
                .uri(AUTH_SERVICE_URL + "/auth/check-email?email={email}", email)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(e -> {
                    System.err.println("Error checking email availability: " + e.getMessage());
                    return Mono.just(false);
                });
    }
}
