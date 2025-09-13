package ru.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.dao.CashOperationDao;

import java.util.UUID;

/**
 * Репозиторий операций
 */
@Repository
public interface CashOperationRepository extends ReactiveCrudRepository<CashOperationDao, UUID> {
}
