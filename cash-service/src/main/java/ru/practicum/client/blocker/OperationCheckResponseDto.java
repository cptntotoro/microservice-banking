package ru.practicum.client.blocker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO результата проверки операции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationCheckResponseDto {
    /**
     * Флаг блокировки
     */
    private boolean blocked;

    /**
     * Код причины
     */
    private String reasonCode;

    /**
     * Описание
     */
    private String description;

    /**
     * Оценка риска
     */
    private int riskScore;
}
