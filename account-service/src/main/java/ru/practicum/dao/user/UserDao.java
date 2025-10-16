package ru.practicum.dao.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO пользователя
 */
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDao {
    /**
     * Идентификатор
     */
    @Id
    @Column("user_uuid")
    private UUID uuid;

    /**
     * Логин
     */
    @Column("username")
    private String username;

    /**
     * Хэш пароля
     */
    @Column("password_hash")
    private String passwordHash;

    /**
     * Имя
     */
    @Column("first_name")
    private String firstName;

    /**
     * Фамилия
     */
    @Column("last_name")
    private String lastName;

    /**
     * Email
     */
    @Column("email")
    private String email;

    /**
     * Дата рождения
     */
    @Column("birth_date")
    private LocalDate birthDate;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Флаг активности аккаунта
     */
    @Column("enabled")
    private boolean enabled;

    /**
     * Флаг блокировки аккаунта
     */
    @Column("account_non_locked")
    private boolean accountNonLocked;
}
