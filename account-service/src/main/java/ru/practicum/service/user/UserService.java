package ru.practicum.service.user;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.user.User;

import java.util.UUID;

/**
 * Сервис для работы с пользователями
 */
public interface UserService {

    /**
     * Регистрация нового пользователя
     *
     * @param user Модель пользователя
     * @return Модель зарегистрированного пользователя
     */
    Mono<User> registerUser(User user, String password);

    Mono<Void> addUserRole(UUID userId, String roleName);

    Mono<Void> removeUserRole(UUID userId, String roleName);

    Mono<Boolean> userHasRole(UUID userId, String roleName);

    /**
     * Получение пользователя по идентификатору
     *
     * @param userId Идентификатор пользователя
     * @return Модель пользователя
     */
    Mono<User> getUserByUuid(UUID userId);

    Mono<User> getUserWithRoles(String username);

    /**
     * Получение пользователя по логину
     *
     * @param login Логин пользователя
     * @return Модель пользователя
     */
    Mono<User> getUserByUsername(String login);

    /**
     * Обновление данных пользователя
     *
     * @param userId Идентификатор пользователя
     * @param user Модель с обновленными данными
     * @return Модель обновленного пользователя
     */
    Mono<User> updateUser(UUID userId, User user);

    /**
     * Изменение пароля пользователя
     *
     * @param userId Идентификатор пользователя
     * @param newPassword Новый пароль
     * @return Результат операции
     */
    Mono<Void> changePassword(UUID userId, String newPassword);

    /**
     * Удаление пользователя
     *
     * @param userId Идентификатор пользователя
     * @return Результат операции
     */
    Mono<Void> deleteUser(UUID userId);

    /**
     * Активация пользователя
     *
     * @param userId Идентификатор пользователя
     */
    Mono<Void> activateUser(UUID userId);

    /**
     * Деактивация пользователя
     *
     * @param userId Идентификатор пользователя
     */
    Mono<Void> deactivateUser(UUID userId);

    /**
     * Блокировка пользователя
     *
     * @param userId Идентификатор пользователя
     */
    Mono<Void> lockAccount(UUID userId);

    /**
     * Разблокировка пользователя
     *
     * @param userId Идентификатор пользователя
     */
    Mono<Void> unlockAccount(UUID userId);

    /**
     * Проверка существования пользователя по логину
     *
     * @param login Логин пользователя
     * @return true если пользователь существует
     */
    Mono<Boolean> existsByUsername(String login);

    /**
     * Проверка существования пользователя по email
     *
     * @param email Email пользователя
     * @return true если пользователь существует
     */
    Mono<Boolean> existsByEmail(String email);

    Flux<User> getAllUsers();

    Flux<User> getUsersByStatus(boolean enabled);

    Flux<User> getUsersByLockStatus(boolean locked);
}