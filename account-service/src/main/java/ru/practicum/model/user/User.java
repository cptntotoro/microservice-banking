package ru.practicum.model.user;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private UUID id;

    /**
     * Логин
     */
    private String login;

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

    /**
     * Флаг блокировки аккаунта
     */
    private boolean accountLocked;
}
