package ru.practicum.repository.user;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserDao;

import java.util.UUID;

/**
 * Репозиторий пользователей
 */
public interface UserRepository extends ReactiveCrudRepository<UserDao, UUID> {

    /**
     * Найти пользователя по логину
     *
     * @param login Логин
     * @return DAO пользователя
     */
    Mono<UserDao> findByLogin(String login);

    /**
     * Проверить наличие пользователя по логину
     *
     * @param login Логин
     * @return Да / Нет
     */
    Mono<Boolean> existsByLogin(String login);

    /**
     * Проверить наличие пользователя по email
     *
     * @param email Адрес электронной почты
     * @return Да / Нет
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * Найти пользователей по флагу активности аккаунта
     *
     * @param enabled Флаг активности аккаунта
     * @return Список DAO пользователей
     */
    Flux<UserDao> findByEnabled(boolean enabled);

    /**
     * Найти пользователей по флагу блокировки аккаунта
     *
     * @param accountLocked Флаг блокировки аккаунта
     * @return Список DAO пользователей
     */
    Flux<UserDao> findByAccountLocked(boolean accountLocked);
}
