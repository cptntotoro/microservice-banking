package ru.practicum.client.account.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignUpResponseClientDto {
    private Long userId;
    private String username;
    private String email;
    private String message;
    private LocalDateTime registeredAt;
    private LoginResponseClientDto loginResponse; // Для автоматического логина после регистрации
}
