package ru.practicum.dao.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO курса валют
 */
@Table(name = "exchange_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateDao {

    /**
     * Идентификатор
     */
    @Id
    private UUID id;

    /**
     * Идентификатор базовой валюты
     */
    @Column("base_currency_id")
    private UUID baseCurrencyId;

    /**
     * Идентификатор целевой валюты
     */
    @Column("target_currency_id")
    private UUID targetCurrencyId;

    /**
     * Курс покупки
     */
    @Column("buy_rate")
    private BigDecimal buyRate;

    /**
     * Курс продажи
     */
    @Column("sell_rate")
    private BigDecimal sellRate;

    /**
     * Дата действия курса
     */
    @Column("effective_date")
    private LocalDate effectiveDate;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
