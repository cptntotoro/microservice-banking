package ru.practicum.repository.account;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.account.AccountDao;

import java.util.UUID;

/**
 * Репозиторий счетов
 */
public interface AccountRepository extends ReactiveCrudRepository<AccountDao, UUID> {
    /**
     * Найти счета пользователя по идентификатору
     *
     * @param userId Идентификатор пользователя
     * @return Список DAO счетов
     */
    Flux<AccountDao> findByUserId(UUID userId);

    /**
     * Проверить существование счета по идентификатору пользователя и идентификатору валюты
     *
     * @param userId Идентификатор пользователя
     * @param currencyId Идентификатор валюты
     * @return Да / Нет
     */
    Mono<Boolean> existsByUserIdAndCurrencyId(UUID userId, UUID currencyId);

    /**
     * Найти счет по идентификатору пользователя и идентификатору валюты
     *
     * @param userId Идентификатор пользователя
     * @param currencyId Идентификатор валюты
     * @return DAO счета
     */
    Mono<AccountDao> findByUserIdAndCurrencyId(UUID userId, UUID currencyId);

    /**
     * Проверить существование счета по идентификатору пользователя и идентификатору счета
     *
     * @param userId Идентификатор пользователя
     * @param id Идентификатор счета
     * @return Да / Нет
     */
    Mono<Boolean> existsByUserIdAndId(UUID userId, UUID id);
}
