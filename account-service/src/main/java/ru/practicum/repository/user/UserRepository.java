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
     * Найти пользователя по username
     *
     * @param username Логин
     * @return DAO пользователя
     */
    Mono<UserDao> findByUsername(String username);

    /**
     * Проверить наличие пользователя по username
     *
     * @param username Логин
     * @return Да / Нет
     */
    Mono<Boolean> existsByUsername(String username);

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
     * @param accountNonLocked Флаг блокировки аккаунта
     * @return Список DAO пользователей
     */
    Flux<UserDao> findByAccountNonLocked(boolean accountNonLocked);
}
