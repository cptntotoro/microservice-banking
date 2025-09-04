package ru.practicum.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.dto.user.PasswordChangeDto;
import ru.practicum.dto.user.UserRegistrationDto;
import ru.practicum.dto.user.UserResponseDto;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.service.user.UserService;

import java.util.UUID;

/**
 * Контроллер для работы с пользователями
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    /**
     * Сервис для работы с пользователями
     */
    private final UserService userService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponseDto> registerUser(@Valid @RequestBody UserRegistrationDto userDto) {
        log.info("Регистрация нового пользователя: {}", userDto.getLogin());

        User user = userMapper.registrationDtoToUser(userDto);
        return userService.registerUser(user, userDto.getPassword())
                .map(userMapper::userToResponseDto);
    }

    /**
     * Получение пользователя по идентификатору
     */
    @GetMapping("/{userId}")
    public Mono<UserResponseDto> getUserById(@PathVariable UUID userId) {
        log.info("Получение пользователя по ID: {}", userId);
        return userService.getUserById(userId)
                .map(userMapper::userToResponseDto);
    }

    /**
     * Обновление данных пользователя
     */
    @PutMapping("/{userId}")
    public Mono<UserResponseDto> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserRegistrationDto userDto) {
        log.info("Обновление данных пользователя: {}", userId);

        User user = userMapper.registrationDtoToUser(userDto);
        return userService.updateUser(userId, user)
                .map(userMapper::userToResponseDto);
    }

    /**
     * Изменение пароля пользователя
     */
    @PatchMapping("/{userId}/password")
    public Mono<Void> changePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody PasswordChangeDto passwordDto) {
        log.info("Изменение пароля пользователя: {}", userId);
        return userService.changePassword(userId, passwordDto.getNewPassword());
    }

    /**
     * Удаление пользователя
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable UUID userId) {
        log.info("Удаление пользователя: {}", userId);
        return userService.deleteUser(userId);
    }

    /**
     * Проверка существования логина
     */
    @GetMapping("/check-login")
    public Mono<Boolean> checkLoginExists(@RequestParam String login) {
        log.info("Проверка существования логина: {}", login);
        return userService.existsByLogin(login);
    }

    /**
     * Проверка существования email
     */
    @GetMapping("/check-email")
    public Mono<Boolean> checkEmailExists(@RequestParam String email) {
        log.info("Проверка существования email: {}", email);
        return userService.existsByEmail(email);
    }
}