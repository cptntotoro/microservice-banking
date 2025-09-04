package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private UUID id;

    /**
     * Логин пользователя
     */
    private String login;

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
}
