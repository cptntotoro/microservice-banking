package ru.practicum.client.auth;

import lombok.Data;

import java.util.UUID;

@Data
public class SignUpResponseClientDto {
    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Логин пользователя
     */
    private String username;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Сообщение
     */
    private String message;

    private LoginResponseClientDto loginResponse; // Для автоматического логина после регистрации
}
