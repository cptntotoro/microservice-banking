package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
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
public class User implements UserDetails {
    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Адрес электронной почты
     */
    private String email;

    /**
     * Дата рождения
     */
    private LocalDate birthDate;

    /**
     * Пароль
     */
    private String passwordHash;

    /**
     * Роли
     */
    private List<UserRole> roles;

    /**
     * Флаг активности аккаунта
     */
    private boolean enabled;

    /**
     * Флаг, указывающий, не заблокирована ли учетная запись
     */
    private boolean accountNonLocked;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles != null ? roles : Collections.singletonList(new UserRole("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}