package ru.practicum.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для изменения пароля
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDto {

    /**
     * Новый пароль
     */
    @NotBlank(message = "Новый пароль обязателен для заполнения")
    @Size(min = 6, message = "Новый пароль должен содержать не менее 6 символов")
    private String newPassword;
}
