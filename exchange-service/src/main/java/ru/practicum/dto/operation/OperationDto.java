package ru.practicum.dto.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.operation.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO валютной операции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationDto {
    /**
     * Идентификатор
     */
    private UUID id;

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