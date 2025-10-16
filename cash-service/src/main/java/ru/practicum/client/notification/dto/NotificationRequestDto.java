package ru.practicum.client.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class NotificationRequestDto {
    private String email;
    private String title;
    private String description;
}