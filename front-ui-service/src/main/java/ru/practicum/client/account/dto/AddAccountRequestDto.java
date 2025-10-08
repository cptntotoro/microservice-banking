package ru.practicum.client.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

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
     * Идентификатор пользователя
     */
    @NotNull(message = "ID пользователя обязателен")
    private UUID userId;

    /**
     * Код валюты
     */
    @NotBlank(message = "Код валюты не может быть пустым")
    private String currencyCode;
}
