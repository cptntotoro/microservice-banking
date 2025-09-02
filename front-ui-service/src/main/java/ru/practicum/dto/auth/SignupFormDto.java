package ru.practicum.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO формы регистрации
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupFormDto {
    @NotBlank
    private String surname;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotNull
    @Past
    private LocalDate birthdate;

    @NotBlank
    @Size(min = 4, max = 50)
    private String login;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String confirmPassword;
}
