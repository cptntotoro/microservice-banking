package ru.practicum.repository.user;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserDao;

import java.util.UUID;

/**
 * Репозиторий пользователей
 */
public interface UserRepository extends ReactiveCrudRepository<UserDao, UUID> {

    Mono<UserDao> findByLogin(String login);

    Mono<UserDao> findByEmail(String email);

    Mono<Boolean> existsByLogin(String login);

    Mono<Boolean> existsByEmail(String email);

    @Query("SELECT * FROM users WHERE created_at >= NOW() - INTERVAL '30 days'")
    Flux<UserDao> findRecentUsers();
}
