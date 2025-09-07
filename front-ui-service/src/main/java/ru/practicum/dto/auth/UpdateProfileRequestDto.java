package ru.practicum.dto.auth;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class UpdateProfileRequestDto {
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустым")
    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private String lastName;

    @Email(message = "Некорректный email адрес")
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    @Past(message = "Дата рождения должна быть в прошлом")
    @NotNull(message = "Дата рождения обязательна")
    private LocalDate birthDate;
}
