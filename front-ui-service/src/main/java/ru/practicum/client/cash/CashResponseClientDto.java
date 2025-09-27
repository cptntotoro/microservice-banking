package ru.practicum.client.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Клиентский DTO ответа на запрос на пополнение счета
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashResponseClientDto {
    /**
     * Статус
     */
    private String status;

    /**
     * Сообщение
     */
    private String message;
}
