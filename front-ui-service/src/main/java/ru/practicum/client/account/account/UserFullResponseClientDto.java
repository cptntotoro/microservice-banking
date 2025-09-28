package ru.practicum.client.account.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Полное DTO пользователя с данными аккаунта и счетами
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFullResponseClientDto {

    /**
     * Данные пользователя
     */
    private UserResponseClientDto user;

    /**
     * Список счетов пользователя
     */
    private List<AccountResponseClientDto> accounts;
}