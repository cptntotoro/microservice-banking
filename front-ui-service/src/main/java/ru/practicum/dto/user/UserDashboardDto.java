package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.account.AccountDashboardDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO для отображения данных пользователя на дашборде
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardDto {

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
     * Список счетов пользователя
     */
    private List<AccountDashboardDto> accounts;
}