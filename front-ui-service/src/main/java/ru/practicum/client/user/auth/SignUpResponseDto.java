package ru.practicum.client.user.auth;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignUpResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String message;
    private LocalDateTime registeredAt;
    private LoginResponseDto loginResponse; // Для автоматического логина после регистрации
}
