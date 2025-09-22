package ru.practicum.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class AccountCreateDto {
    /**
     * Идентификатор пользователя
     */
    @NotNull(message = "ID пользователя обязателен")
    private UUID userId;

    /**
     * Идентификатор валюты
     */
    @NotNull(message = "Валюта обязательна для выбора")
    private UUID currencyId;

    /**
     * Номер счета
     */
    @NotBlank(message = "Номер счета обязателен для заполнения")
    @Size(max = 20, message = "Номер счета должен содержать не более 20 символов")
    private String accountNumber;
}
