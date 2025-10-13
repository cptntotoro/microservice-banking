package ru.practicum.repository.account;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.account.AccountDao;

import java.util.UUID;

/**
 * Репозиторий счетов
 */
public interface AccountRepository extends ReactiveCrudRepository<AccountDao, UUID> {

    Flux<AccountDao> findByUserId(UUID userId);

    Mono<Boolean> existsByUserIdAndCurrencyId(UUID userId, UUID currencyId);

    Mono<AccountDao> findByUserIdAndCurrencyId(UUID userId, UUID currencyId);

    Mono<Boolean> existsByUserIdAndId(UUID userId, UUID id);
}
