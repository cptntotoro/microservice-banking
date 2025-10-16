package ru.practicum.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private Long expiresIn;

    public AuthResponse(String accessToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}