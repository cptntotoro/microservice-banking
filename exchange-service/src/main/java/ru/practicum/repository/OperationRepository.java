package ru.practicum.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.OperationDao;

import java.util.UUID;

public interface OperationRepository extends ReactiveCrudRepository<OperationDao, UUID> {
    /**
     * Получить недавние операции
     *
     * @param limit Число операций
     * @return DAO валютной операции
     */
    @Query("SELECT * FROM operations ORDER BY created_at DESC LIMIT :limit")
    Flux<OperationDao> findRecentOperations(int limit);

    /**
     * Получить операции по исходной и целевой валютам
     *
     * @param fromCurrency Исходная валюта
     * @param toCurrency Целевая валюта
     * @return DAO валютной операции
     */
    Flux<OperationDao> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);

    /**
     * Получить все используемые валюты
     *
     * @return Список кодов валют
     */
    @Query("SELECT DISTINCT from_currency FROM operations UNION SELECT DISTINCT to_currency FROM operations")
    Flux<String> findAllUsedCurrencies();
}