package ru.practicum.service.auth;

import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.dto.SignUpResponseDto;
import ru.practicum.client.auth.dto.TokenResponseDto;
import ru.practicum.dto.auth.LoginRequestDto;
import ru.practicum.dto.auth.SignUpRequestDto;

public interface AuthService {
    Mono<SignUpResponseDto> createUser(SignUpRequestDto signUpRequestDto);

    Mono<TokenResponseDto> login(LoginRequestDto loginRequest);

    void logout(WebSession session);

    Mono<String> getUserId(String token);
}
