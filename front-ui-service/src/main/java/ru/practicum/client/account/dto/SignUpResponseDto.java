package ru.practicum.client.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO ответа клиента сервиса аккаунтов после регистрации пользователя
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponseDto {
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
