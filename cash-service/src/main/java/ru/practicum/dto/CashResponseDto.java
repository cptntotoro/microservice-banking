package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ответа на запрос на пополнение счета
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashResponseDto {
    /**
     * Статус
     */
    private String status;

    /**
     * Сообщение
     */
    private String message;
}
