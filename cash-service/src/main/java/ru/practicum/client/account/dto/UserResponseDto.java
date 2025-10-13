package ru.practicum.client.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO для ответа с данными пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    /**
     * Идентификатор пользователя
     */
    private UUID uuid;

    /**
     * Логин пользователя
     */
    private String username;

    /**
     * Имя пользователя
     */
    private String firstName;

    /**
     * Фамилия пользователя
     */
    private String lastName;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Дата рождения пользователя
     */
    private LocalDate birthDate;

    /**
     * Дата создания аккаунта
     */
    private LocalDateTime createdAt;

    /**
     * Роли
     */
    private List<String> roles;
}
