package ru.practicum.client.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Запрос на валидацию токена
 */
@AllArgsConstructor
@Data
public class TokenValidationRequestDto {
    /**
     * Токен
     */
    @NotBlank
    private String token;
}
