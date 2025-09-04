package ru.practicum.dao.currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO валюты
 */
@Table(name = "currencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyDao {

    /**
     * Идентификатор
     */
    @Id
    private UUID id;

    /**
     * Код валюты (USD, EUR, RUB)
     */
    private String code;

    /**
     * Название валюты
     */
    private String name;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
