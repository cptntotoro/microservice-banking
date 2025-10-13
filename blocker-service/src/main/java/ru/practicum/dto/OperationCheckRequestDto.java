package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO запроса на проверку операции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationCheckRequestDto {
    /**
     * Идентификатор операции
     */
    private UUID operationId;

    /**
     * Тип операции
     */
    private String operationType; // "DEPOSIT", "WITHDRAW", "TRANSFER"

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Валюта операции
     */
    private String currency;

    /**
     * Время операции
     */
    private LocalDateTime timestamp;
}
