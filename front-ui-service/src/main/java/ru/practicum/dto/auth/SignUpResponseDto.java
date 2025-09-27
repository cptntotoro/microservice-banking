package ru.practicum.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.client.account.user.LoginResponseClientDto;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String message;
    private LocalDateTime registeredAt;
    private LoginResponseClientDto loginResponse; // Для автоматического логина после регистрации
}
