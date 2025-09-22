package ru.practicum.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteUserProfileDto {
    /**
     * Логин пользователя для удаления.
     */
    @NotBlank(message = "Логин обязателен")
    private String login;
}
