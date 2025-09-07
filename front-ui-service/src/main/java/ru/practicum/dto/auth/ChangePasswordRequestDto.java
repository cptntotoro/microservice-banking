package ru.practicum.dto.auth;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ChangePasswordRequestDto {
    @NotBlank(message = "Текущий пароль не может быть пустым")
    private String currentPassword;

    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 6, message = "Новый пароль должен содержать минимум 6 символов")
    private String newPassword;

    @NotBlank(message = "Подтверждение пароля не может быть пустым")
    private String confirmPassword;
}
