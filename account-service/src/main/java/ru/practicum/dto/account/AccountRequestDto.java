package ru.practicum.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для создания счета
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
