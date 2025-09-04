package ru.practicum.repository.account;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.account.AccountDao;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Репозиторий счетов
 */
public interface AccountRepository extends ReactiveCrudRepository<AccountDao, UUID> {

    Flux<AccountDao> findByUserId(UUID userId);

    Mono<AccountDao> findByAccountNumber(String accountNumber);

    Mono<Boolean> existsByUserIdAndCurrencyId(UUID userId, UUID currencyId);

    @Query("SELECT a.* FROM accounts a WHERE a.user_id = :userId AND a.balance > 0")
    Flux<AccountDao> findNonEmptyAccountsByUserId(UUID userId);

    @Query("SELECT SUM(balance) FROM accounts WHERE user_id = :userId")
    Mono<BigDecimal> getTotalBalanceByUserId(UUID userId);
}
