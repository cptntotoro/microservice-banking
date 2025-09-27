package ru.practicum.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO для удаления аккаунта пользователя
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserProfileDto {

    @NotBlank(message = "Логин обязателен")
    private String login;
}
