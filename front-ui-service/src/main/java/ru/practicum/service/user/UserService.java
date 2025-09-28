package ru.practicum.service.user;

import reactor.core.publisher.Mono;
import ru.practicum.model.user.UserWithAccounts;

import java.util.UUID;

/**
 * Сервис управления пользователями
 */
public interface UserService {

    /**
     * Получить пользователя с его счетами
     *
     * @param userId Идентификатор пользователя
     * @return Пользователь с его счетами
     */
    Mono<UserWithAccounts> getUserWithAccounts(UUID userId);
}