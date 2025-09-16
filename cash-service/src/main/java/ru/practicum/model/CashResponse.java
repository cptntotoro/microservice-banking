package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ на запрос на пополнение счета
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashResponse {
    /**
     * Статус
     */
    private String status;

    /**
     * Сообщение
     */
    private String message;
}
