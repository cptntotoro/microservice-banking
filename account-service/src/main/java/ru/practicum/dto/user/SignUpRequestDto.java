package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для регистрации пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {
    /**
     * Логин пользователя
     */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 5, max = 15, message = "Имя пользователя должно содержать 5-15 символа")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,15}$", message = "Имя пользователя может содержать только буквы, цифры и подчеркивания")
    private String username;

    /**
     * Пароль пользователя
     */
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String password;

    /**
     * Имя пользователя
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String firstName;

    /**
     * Фамилия пользователя
     */
    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private String lastName;

    /**
     * Email пользователя
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    @Size(max = 50, message = "Email должен содержать не более 50 символов")
    private String email;

    /**
     * Дата рождения пользователя
     */
    @NotNull(message = "Дата рождения не может быть пустой")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthDate;
}