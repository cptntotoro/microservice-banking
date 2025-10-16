package ru.practicum.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO запроса на добавление счета в новой валюте
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddAccountRequestDto {
    /**
     * Код валюты
     */
    @NotBlank(message = "Код валюты не может быть пустым")
    private String currencyCode;
}
