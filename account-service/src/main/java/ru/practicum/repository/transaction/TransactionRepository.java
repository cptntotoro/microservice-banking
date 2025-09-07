package ru.practicum.repository.transaction;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.transaction.TransactionDao;
import ru.practicum.model.transaction.TransactionStatus;
import ru.practicum.model.transaction.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Репозиторий транзакций
 */
public interface TransactionRepository extends ReactiveCrudRepository<TransactionDao, UUID> {

    Flux<TransactionDao> findByFromAccountId(UUID fromAccountId);

    Flux<TransactionDao> findByToAccountId(UUID toAccountId);

    Flux<TransactionDao> findByType(TransactionType type);

    Flux<TransactionDao> findByStatus(TransactionStatus status);

    @Query("SELECT t.* FROM transactions t " +
            "WHERE (t.from_account_id = :accountId OR t.to_account_id = :accountId) " +
            "ORDER BY t.created_at DESC " +
            "LIMIT :limit")
    Flux<TransactionDao> findRecentTransactionsByAccountId(UUID accountId, int limit);

    @Query("SELECT t.* FROM transactions t " +
            "WHERE t.created_at BETWEEN :startDate AND :endDate " +
            "AND (t.from_account_id = :accountId OR t.to_account_id = :accountId)")
    Flux<TransactionDao> findByAccountIdAndPeriod(UUID accountId, LocalDateTime startDate, LocalDateTime endDate);

    Flux<TransactionDao> findByFromAccountIdOrToAccountId(UUID accountId, UUID accountId1);
}
