package ru.practicum.repository.exchange;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.exchange.ExchangeRateDao;

import java.time.LocalDate;
import java.util.UUID;

public interface ExchangeRateRepository extends ReactiveCrudRepository<ExchangeRateDao, UUID> {

    Mono<ExchangeRateDao> findByBaseCurrencyIdAndTargetCurrencyIdAndEffectiveDate(
            UUID baseCurrencyId, UUID targetCurrencyId, LocalDate effectiveDate);

    Flux<ExchangeRateDao> findByEffectiveDate(LocalDate effectiveDate);

    @Query("SELECT er.* FROM exchange_rates er " +
            "WHERE er.effective_date = :date " +
            "ORDER BY er.base_currency_id, er.target_currency_id")
    Flux<ExchangeRateDao> findLatestRates(LocalDate date);
}
