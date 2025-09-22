package ru.practicum.client.account.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.Currency;

import java.math.BigDecimal;
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
    private UUID userId;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private String birthdate;
    private List<AccountDto> accounts;
    private List<Currency> availableCurrencies;
    private List<UserDto> otherUsers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDto {
        private UUID id;
        private Currency currency;
        private BigDecimal balance;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private String login;
        private String name;
    }
}
