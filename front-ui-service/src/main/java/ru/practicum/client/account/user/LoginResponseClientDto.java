package ru.practicum.client.account.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.client.account.account.AccountResponseClientDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO ответа на логин
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseClientDto {
    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Имя пользователя
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
     * Email пользователя
     */
    private String email;

    /**
     * Дата рождения
     */
    private LocalDate birthDate;

    /**
     * Счета
     */
    private List<AccountResponseClientDto> accounts;

    /**
     * Доступные валюты
     */
    private List<String> availableCurrencies;
}
