package ru.practicum.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.user.UserResponseDto;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.service.user.UserService;

import java.util.UUID;

/**
 * Административный контроллер для управления пользователями
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    /**
     * Сервис для работы с пользователями
     */
    private final UserService userService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    /**
     * Активация пользователя
     */
    @PatchMapping("/{userId}/activate")
    public Mono<Void> activateUser(@PathVariable UUID userId) {
        log.info("Активация пользователя: {}", userId);
        return userService.activateUser(userId);
    }

    /**
     * Деактивация пользователя
     */
    @PatchMapping("/{userId}/deactivate")
    public Mono<Void> deactivateUser(@PathVariable UUID userId) {
        log.info("Деактивация пользователя: {}", userId);
        return userService.deactivateUser(userId);
    }

    /**
     * Блокировка аккаунта пользователя
     */
    @PatchMapping("/{userId}/lock")
    public Mono<Void> lockAccount(@PathVariable UUID userId) {
        log.info("Блокировка аккаунта пользователя: {}", userId);
        return userService.lockAccount(userId);
    }

    /**
     * Разблокировка аккаунта пользователя
     */
    @PatchMapping("/{userId}/unlock")
    public Mono<Void> unlockAccount(@PathVariable UUID userId) {
        log.info("Разблокировка аккаунта пользователя: {}", userId);
        return userService.unlockAccount(userId);
    }

    /**
     * Добавить роль пользователю
     */
    @PostMapping("/{userId}/roles/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> addUserRole(@PathVariable UUID userId, @PathVariable String roleName) {
        log.info("Добавление роли {} пользователю: {}", roleName, userId);
        return userService.addUserRole(userId, roleName);
    }

    /**
     * Удалить роль у пользователя
     */
    @DeleteMapping("/{userId}/roles/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeUserRole(@PathVariable UUID userId, @PathVariable String roleName) {
        log.info("Удаление роли {} у пользователя: {}", roleName, userId);
        return userService.removeUserRole(userId, roleName);
    }

    /**
     * Получение всех пользователей
     */
    @GetMapping
    public Flux<UserResponseDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return userService.getAllUsers()
                .map(userMapper::userToResponseDto);
    }

    /**
     * Получение пользователей по статусу активности
     */
    @GetMapping("/by-status")
    public Flux<UserResponseDto> getUsersByStatus(@RequestParam boolean enabled) {
        log.info("Получение пользователей по статусу активности: {}", enabled);
        return userService.getUsersByStatus(enabled)
                .map(userMapper::userToResponseDto);
    }

    /**
     * Получение пользователей по статусу блокировки
     */
    @GetMapping("/by-lock-status")
    public Flux<UserResponseDto> getUsersByLockStatus(@RequestParam boolean locked) {
        log.info("Получение пользователей по статусу блокировки: {}", locked);
        return userService.getUsersByLockStatus(locked)
                .map(userMapper::userToResponseDto);
    }
}