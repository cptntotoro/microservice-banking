package ru.practicum.client.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для обращений к сервису оповещений
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Сообщение
     */
    private String message;
}