package ru.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.dao.TransferDao;

import java.util.UUID;

/**
 * Репозиторий операций перевода средств
 */
@Repository
public interface TransferRepository extends ReactiveCrudRepository<TransferDao, UUID> {
}
