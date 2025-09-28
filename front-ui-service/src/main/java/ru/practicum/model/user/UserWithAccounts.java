package ru.practicum.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.account.Account;

import java.util.List;

/**
 * Пользователь с его счетами
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWithAccounts {

    /**
     * Пользователь
     */
    private User user;

    /**
     * Список счетов
     */
    private List<Account> accounts;
}
