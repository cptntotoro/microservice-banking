package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Результат проверки операции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationCheckResponse {
    /**
     * Флаг блокировки операции
     */
    private boolean blocked;

    /**
     * Код причины блокировки
     */
    private BlockReasonCode reasonCode;

    /**
     * Описание причины блокировки
     */
    private String description;

    /**
     * Оценка риска (0-100+)
     */
    private int riskScore;
}
