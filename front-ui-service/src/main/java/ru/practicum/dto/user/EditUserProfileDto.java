package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO редактирования профиля пользователя
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditUserProfileDto {

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
