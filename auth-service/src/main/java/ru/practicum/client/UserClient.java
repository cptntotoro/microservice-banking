//package ru.practicum.client;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import ru.practicum.mapper.UserMapper;
//import ru.practicum.model.User;
//
//@Component
//@RequiredArgsConstructor
//public class UserClient {
//
//    private final WebClient.Builder webClientBuilder;
//
//    private static final String USER_SERVICE_URL = "http://account-service";
//    private final UserMapper userMapper;
//
//    public Mono<User> getUserByUsername(String login) {
//        return webClientBuilder.build()
//                .get()
//                .uri(USER_SERVICE_URL + "/api/users/by-login/{login}", login)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .retrieve()
//                .bodyToMono(UserResponseDto.class)
//                .map(userMapper::userResponseDtoToUser)
//                .onErrorResume(e -> {
//                    // Логирование ошибки
//                    System.err.println("Error fetching user by login: " + e.getMessage());
//                    return Mono.empty();
//                });
//    }
//
//    public Mono<User> getUserById(String userId) {
//        return webClientBuilder.build()
//                .get()
//                .uri(USER_SERVICE_URL + "/api/users/{userId}", userId)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .retrieve()
//                .bodyToMono(UserResponseDto.class)
//                .map(userMapper::userResponseDtoToUser);
//    }
//}