package ru.practicum.client.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
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