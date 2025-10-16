package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Запрос на валидацию токена
 */
@AllArgsConstructor
@Data
public class TokenValidationRequest {
    /**
     * Токен
     */
    @NotBlank
    private String token;
}
