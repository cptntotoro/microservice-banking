package ru.practicum.client.auth.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserProfileResponseClientDto {
    /**
     * Идентификатор пользователя
     */
    private UUID id;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Дата рождения
     */
    private LocalDate birthDate;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата последнего логина
     */
    private LocalDateTime lastLoginAt;

    /**
     * Флаг подтверждения адреса электронной почты
     */
    private Boolean emailVerified;

    /**
     * Флаг активности аккаунта
     */
    private Boolean accountEnabled;
}
