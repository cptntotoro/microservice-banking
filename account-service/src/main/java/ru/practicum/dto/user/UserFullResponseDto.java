package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.account.AccountResponseDto;

import java.util.List;

/**
 * Полное DTO пользователя с данными аккаунта и счетами
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFullResponseDto {

    /**
     * Данные пользователя
     */
    private UserResponseDto user;

    /**
     * Список счетов пользователя
     */
    private List<AccountResponseDto> accounts;
}