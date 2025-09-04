package ru.practicum.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Принудительная активация пользователя (админ)
     */
    @PatchMapping("/{userId}/force-activate")
    public Mono<Void> forceActivateUser(@PathVariable UUID userId) {
        log.info("Принудительная активация пользователя: {}", userId);
        return userService.activateUser(userId);
    }

    /**
     * Принудительная деактивация пользователя (админ)
     */
    @PatchMapping("/{userId}/force-deactivate")
    public Mono<Void> forceDeactivateUser(@PathVariable UUID userId) {
        log.info("Принудительная деактивация пользователя: {}", userId);
        return userService.deactivateUser(userId);
    }

    /**
     * Принудительная блокировка аккаунта (админ)
     */
    @PatchMapping("/{userId}/force-lock")
    public Mono<Void> forceLockAccount(@PathVariable UUID userId) {
        log.info("Принудительная блокировка аккаунта: {}", userId);
        return userService.lockAccount(userId);
    }

    /**
     * Принудительная разблокировка аккаунта (админ)
     */
    @PatchMapping("/{userId}/force-unlock")
    public Mono<Void> forceUnlockAccount(@PathVariable UUID userId) {
        log.info("Принудительная разблокировка аккаунта: {}", userId);
        return userService.unlockAccount(userId);
    }
}