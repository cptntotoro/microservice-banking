package ru.practicum.dto;

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
    private UUID userId;

    /**
     * Сообщение
     */
    private String message;
}