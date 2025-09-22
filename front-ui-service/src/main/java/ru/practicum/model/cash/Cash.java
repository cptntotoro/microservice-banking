package ru.practicum.model.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Операция с наличными
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cash {
    /**
     * Идентификатор операции
     */
    private UUID operationId;

    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Тип операции
     */
    private String operationType;
    private BigDecimal amount;
    private String currency;
    private BigDecimal newBalance;
    private LocalDateTime operationDate;
    private String status;
    private String message;
}