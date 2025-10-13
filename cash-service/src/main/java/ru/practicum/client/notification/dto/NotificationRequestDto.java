package ru.practicum.client.notification.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class NotificationRequestDto {
    private String email;
    private String title;
    private String description;
}