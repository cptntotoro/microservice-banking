package ru.practicum.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.OperationRecordDao;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Репозиторий операций
 */
@Repository
public interface OperationRecordRepository extends ReactiveCrudRepository<OperationRecordDao, Long> {
    /**
     * Получить историю операций пользователя
     *
     * @param userId Идентификатор пользователя
     * @return Список операций пользователя
     */
    @Query("SELECT * FROM operation_records WHERE user_id = :userId ORDER BY timestamp DESC")
    Flux<OperationRecordDao> findByUserId(UUID userId);

    /**
     * Получить число операций пользователя с даты и времени
     *
     * @param userId Идентификатор пользователя
     * @param since Дата и время периода (включительно)
     * @return Число операций
     */
    @Query("SELECT COUNT(*) FROM operation_records WHERE user_id = :userId AND timestamp >= :since")
    Mono<Integer> countOperationsByUserSince(UUID userId, LocalDateTime since);

    /**
     * Получить среднее значение суммы операций по пользователю и типу операции
     *
     * @param userId Идентификатор пользователя
     * @param operationType Тип операции
     * @return Среднее значение суммы операций
     */
    @Query("SELECT AVG(amount) FROM operation_records WHERE user_id = :userId AND operation_type = :operationType")
    Mono<Double> findAverageAmountByUserAndType(UUID userId, String operationType);

    /**
     * Проверить существование операции по идентификатору
     *
     * @param operationId Идентификатор операции
     * @return Да / Нет
     */
    Mono<Boolean> existsByOperationId(UUID operationId);

    /**
     * Получить количество заблокированных операций пользователя за период
     *
     * @param userId Идентификатор пользователя
     * @param since Дата и время периода (включительно)
     * @return Количество заблокированных операций
     */
    @Query("SELECT COUNT(*) FROM operation_records WHERE user_id = :userId AND blocked = true AND timestamp >= :since")
    Mono<Integer> countBlockedOperationsByUserSince(UUID userId, LocalDateTime since);
}
