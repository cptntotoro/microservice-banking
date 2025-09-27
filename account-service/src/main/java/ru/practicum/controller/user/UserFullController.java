package ru.practicum.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.user.UserFullResponseDto;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.service.user.UserService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users/full")
@RequiredArgsConstructor
public class UserFullController {
    /**
     * Сервис для работы с пользователями
     */
    private final UserService userService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @GetMapping("/{userId}")
    public Mono<UserFullResponseDto> getFullUser(@PathVariable UUID userId) {
        log.info("Получение полной информации о пользователе по ID: {}", userId);
        return userService.getUserWithAccountsByUuid(userId)
                .map(userMapper::userWithAccountsToFullResponseDto);
    }
}