package ru.practicum.dto.user;

import jakarta.validation.constraints.*;
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
public class UserSignUpDto {

    /**
     * Логин пользователя
     */
    @NotBlank(message = "Username  обязателен для заполнения")
    @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
    private String username;

    /**
     * Пароль пользователя
     */
    @NotBlank(message = "Пароль обязателен для заполнения")
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String password;

    /**
     * Имя пользователя
     */
    @NotBlank(message = "Имя обязательно для заполнения")
    @Size(max = 30, message = "Имя должно содержать не более 30 символов")
    private String firstName;

    /**
     * Фамилия пользователя
     */
    @NotBlank(message = "Фамилия обязательна для заполнения")
    @Size(max = 30, message = "Фамилия должна содержать не более 30 символов")
    private String lastName;

    /**
     * Email пользователя
     */
    @NotBlank(message = "Email обязателен для заполнения")
    @Email(message = "Email должен быть валидным")
    @Size(max = 50, message = "Email должен содержать не более 50 символов")
    private String email;

    /**
     * Дата рождения пользователя
     */
    @NotNull(message = "Дата рождения обязательна для заполнения")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthDate;

    @AssertTrue(message = "Необходимо согласие с условиями")
    private Boolean termsAccepted;
}