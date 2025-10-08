package ru.practicum.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ответа клиента на операцию с наличными
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashResponseDto {
    /**
     * Статус операции
     */
    private String status;

    /**
     * Сообщение
     */
    private String message;
}
