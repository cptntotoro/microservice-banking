package ru.practicum.dto.auth;

import lombok.Data;
import ru.practicum.client.account.user.LoginResponseClientDto;

import java.time.LocalDateTime;

@Data
public class SignUpResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String message;
    private LocalDateTime registeredAt;
    private LoginResponseClientDto loginResponse; // Для автоматического логина после регистрации
}
