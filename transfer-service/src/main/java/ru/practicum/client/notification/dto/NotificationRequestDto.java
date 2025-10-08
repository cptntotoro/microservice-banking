package ru.practicum.client.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для обращений к сервису оповещений
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
    /**
     * Идентификатор пользователя
     */
    //TODO WWWWWWHYYYYYYYY userId? accountId? не?
    private UUID userId;

    /**
     * Сообщение
     */
    private String message;
}