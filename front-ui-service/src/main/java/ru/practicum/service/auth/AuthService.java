package ru.practicum.service.auth;

import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.dto.SignUpResponseDto;
import ru.practicum.client.auth.dto.TokenResponseDto;
import ru.practicum.dto.auth.LoginRequestDto;
import ru.practicum.dto.auth.SignUpRequestDto;

/**
 * Сервис авторизации
 */
public interface AuthService {

    /**
     * Создать пользователя
     *
     * @param signUpRequestDto DTO формы регистрации
     * @return DTO ответа клиента сервиса аккаунтов после регистрации пользователя
     */
    Mono<SignUpResponseDto> createUser(SignUpRequestDto signUpRequestDto);

    /**
     * Залогинить пользователя
     *
     * @param loginRequest DTO формы логина
     * @return DTO ответа на валидацию токена
     */
    Mono<TokenResponseDto> login(LoginRequestDto loginRequest);

    /**
     * Разлогинить пользователя
     *
     * @param session Сессия
     */
    void logout(WebSession session);

    /**
     * Получить идентификатор пользователя из токена
     *
     * @param token Токен
     * @return
     */
    Mono<String> getUserId(String token);
}
