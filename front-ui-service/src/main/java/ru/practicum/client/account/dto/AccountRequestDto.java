package ru.practicum.client.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO запроса на добавление счета в новой валюте
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequestDto {

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
