package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.AuthRequest;
import ru.practicum.dto.AuthResponse;
import ru.practicum.dto.TokenValidationRequest;
import ru.practicum.service.AuthService;

import java.util.UUID;

/**
 * Контроллер для обработки запросов аутентификации и авторизации
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Аутентификация пользователя и выдача JWT-токена
     * @param request DTO с логином и паролем
     * @return Mono с ResponseEntity, содержащим токен и время жизни
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        return authService.authenticate(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    /**
     * Валидация JWT-токена
     * @param request DTO с токеном
     * @return Mono с результатом валидации (true/false)
     */
    @PostMapping("/validate")
    public Mono<ResponseEntity<Boolean>> validateToken(@Valid @RequestBody TokenValidationRequest request) {
        return authService.validateToken(request.getToken())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false)));
    }

    @PostMapping("/getUserId")
    public Mono<ResponseEntity<String>> getUserId(@Valid @RequestBody TokenValidationRequest request) {
        return authService.getUserId(request.getToken())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("false")));
    }
}