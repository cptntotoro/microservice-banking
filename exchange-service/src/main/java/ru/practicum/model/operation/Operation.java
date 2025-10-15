package ru.practicum.model.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Валютная операция
 */
@Table("operations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Код исходной валюты
     */
    private String fromCurrency;

    /**
     * Код целевой валюты
     */
    private String toCurrency;

    /**
     * Сумма исходной валюты
     */
    private BigDecimal amount;

    /**
     * Сумма целевой валюты
     */
    private BigDecimal convertedAmount;

    /**
     * Курс обмена
     */
    private BigDecimal exchangeRate;

    /**
     * Тип операции
     */
    private OperationType operationType;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}