package ru.practicum.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private String email;
    private String title;
    private String description;
}
