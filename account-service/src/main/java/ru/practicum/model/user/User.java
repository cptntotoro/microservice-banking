package ru.practicum.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Пользователь
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Логин
     */
    private String username;

    /**
     * Хэш пароля
     */
    private String passwordHash;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Email
     */
    private String email;

    /**
     * Дата рождения
     */
    private LocalDate birthDate;

    /**
     * Роли
     */
    private List<String> roles;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    private LocalDateTime updatedAt;

    /**
     * Флаг активности аккаунта
     */
    private boolean enabled;

    /**
     * Флаг, указывающий, не заблокирована ли учетная запись
     */
    private boolean accountNonLocked;
}
