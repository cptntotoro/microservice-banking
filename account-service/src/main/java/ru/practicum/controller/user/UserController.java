package ru.practicum.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.user.PasswordChangeDto;
import ru.practicum.dto.user.UserResponseDto;
import ru.practicum.dto.user.UserSignUpDto;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.service.user.UserService;

import java.util.UUID;

/**
 * Контроллер для работы с пользователями
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
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

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponseDto> registerUser(@Valid @RequestBody UserSignUpDto userDto) {
        User user = userMapper.registrationDtoToUser(userDto);
        return userService.registerUser(user, userDto.getPassword())
                .map(userMapper::userToResponseDto);
    }

    @GetMapping("/{userId}")
    public Mono<UserResponseDto> getUserById(@PathVariable UUID userId) {
        log.info("Получение пользователя по ID: {}", userId);
        return userService.getUserByUuid(userId)
                .map(userMapper::userToResponseDto);
    }

    @GetMapping("/by-username/{username}")
    public Mono<UserResponseDto> getUserByUsername(@PathVariable String username) {
        log.info("Получение пользователя по username: {}", username);
        return userService.getUserByUsername(username)
                .map(userMapper::userToResponseDto);
    }

    @GetMapping("/{userId}/has-role/{roleName}")
    public Mono<Boolean> userHasRole(@PathVariable UUID userId, @PathVariable String roleName) {
        log.info("Проверка роли {} у пользователя: {}", roleName, userId);
        return userService.userHasRole(userId, roleName);
    }

    @PutMapping("/{userId}")
    public Mono<UserResponseDto> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserSignUpDto userDto) {
        log.info("Обновление данных пользователя: {}", userId);

        User user = userMapper.registrationDtoToUser(userDto);
        return userService.updateUser(userId, user)
                .map(userMapper::userToResponseDto);
    }

    @PatchMapping("/{userId}/password")
    public Mono<Void> changePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody PasswordChangeDto passwordDto) {
        log.info("Изменение пароля пользователя: {}", userId);
        return userService.changePassword(userId, passwordDto.getNewPassword());
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable UUID userId) {
        log.info("Удаление пользователя: {}", userId);
        return userService.deleteUser(userId);
    }

    @GetMapping("/check-login")
    public Mono<Boolean> checkLoginExists(@RequestParam String login) {
        log.info("Проверка существования логина: {}", login);
        return userService.existsByUsername(login);
    }

    @GetMapping("/check-email")
    public Mono<Boolean> checkEmailExists(@RequestParam String email) {
        log.info("Проверка существования email: {}", email);
        return userService.existsByEmail(email);
    }
}