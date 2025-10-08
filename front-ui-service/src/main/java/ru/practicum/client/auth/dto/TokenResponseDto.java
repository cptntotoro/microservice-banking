package ru.practicum.client.auth.dto;

import lombok.Data;

@Data
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}