package ru.practicum.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.OperationDao;

public interface OperationRepository extends ReactiveCrudRepository<OperationDao, Long> {

    @Query("SELECT * FROM operations ORDER BY created_at DESC LIMIT :limit")
    Flux<OperationDao> findRecentOperations(int limit);

    Flux<OperationDao> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);

    @Query("SELECT DISTINCT from_currency FROM operations UNION SELECT DISTINCT to_currency FROM operations")
    Flux<String> findAllUsedCurrencies();
}