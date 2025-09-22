package ru.practicum.client.account.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO для отрисовки дашборда
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDataClientDto {
    /**
     * Логин пользователя.
     */
    private String login;

    /**
     * Имя пользователя.
     */
    private String firstName;

    /**
     * Фамилия пользователя.
     */
    private String lastName;

    /**
     * Электронная почта пользователя.
     */
    private String email;

    /**
     * Дата рождения пользователя.
     */
    private LocalDate birthDate;

    /**
     * Список счетов пользователя.
     */
    private List<AccountClientDto> accounts;

    /**
     * Список доступных валют для добавления новых счетов.
     */
    private List<CurrencyClientDto> availableCurrencies;
}
