package ru.practicum.dto.account;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO для запроса на удаление счета.
 */
public class DeleteAccountRequestDTO {

    /**
     * Идентификатор счета для удаления.
     */
    @NotNull(message = "ID счета обязателен")
    private UUID accountId;
}
