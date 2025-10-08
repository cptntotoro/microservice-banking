package ru.practicum.client.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TokenValidationRequest {
    @NotBlank
    private String token;
}
