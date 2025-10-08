package ru.practicum.repository.currency;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.practicum.dao.currency.CurrencyDao;

import java.util.UUID;

/**
 * Репозиторий валют
 */
public interface CurrencyRepository extends ReactiveCrudRepository<CurrencyDao, UUID> {
    /**
     * Найти валюту по идентификатору
     *
     * @param id Идентификатор
     * @return DAO валюты
     */
    Mono<CurrencyDao> findById(UUID id);

    /**
     * Найти валюту по коду
     *
     * @param code Код
     * @return DAO валюты
     */
    Mono<CurrencyDao> findByCode(String code);
}