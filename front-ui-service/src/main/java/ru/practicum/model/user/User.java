package ru.practicum.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Пользователь
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
