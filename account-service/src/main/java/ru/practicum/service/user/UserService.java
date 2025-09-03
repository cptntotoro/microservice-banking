package ru.practicum.service.user;


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

    /**
     * Получение пользователя по идентификатору
     *
     * @param userId Идентификатор пользователя
     * @return Модель пользователя
     */
    Mono<User> getUserById(UUID userId);

    /**
     * Получение пользователя по логину
     *
     * @param login Логин пользователя
     * @return Модель пользователя
     */
    Mono<User> getUserByLogin(String login);

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
     * Проверка существования пользователя по логину
     *
     * @param login Логин пользователя
     * @return true если пользователь существует
     */
    Mono<Boolean> existsByLogin(String login);

    /**
     * Проверка существования пользователя по email
     *
     * @param email Email пользователя
     * @return true если пользователь существует
     */
    Mono<Boolean> existsByEmail(String email);
}