package ru.practicum.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long expiresIn;

    public AuthResponse(String token, Long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }
}