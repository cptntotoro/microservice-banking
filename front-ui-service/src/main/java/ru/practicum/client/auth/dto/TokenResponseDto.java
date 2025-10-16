package ru.practicum.client.auth.dto;

import lombok.Data;

/**
 * DTO ответа на валидацию токена
 */
@Data
public class TokenResponseDto {
    /**
     * Токен доступа
     */
    private String accessToken;

    /**
     * Токен обновления
     */
    private String refreshToken;

    /**
     * Период действия
     */
    private long expiresIn;
}