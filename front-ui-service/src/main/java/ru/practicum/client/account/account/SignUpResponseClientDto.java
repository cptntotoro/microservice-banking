package ru.practicum.client.account.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO ответа клиента сервиса аккаунтов после регистрации пользователя
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponseClientDto {

    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Сообщение
     */
    private String message;
    private AccountResponseClientDto loginResponse;
}
