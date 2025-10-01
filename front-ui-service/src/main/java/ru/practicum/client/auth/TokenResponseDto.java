package ru.practicum.client.auth;

import lombok.Data;

@Data
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}