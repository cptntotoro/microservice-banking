package ru.practicum.client.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TokenValidationRequest {
    @NotBlank
    private String token;
}
