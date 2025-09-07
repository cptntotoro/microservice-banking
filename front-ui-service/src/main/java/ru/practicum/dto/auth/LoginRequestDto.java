package ru.practicum.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO формы логина
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    @Builder.Default
    private Boolean rememberMe = false;
}