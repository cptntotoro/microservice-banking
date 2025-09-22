package ru.practicum.dto.cash;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для запроса на операции с наличными (внесение/снятие).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashRequestDto {
    /**
     * Идентификатор счета
     */
    @NotNull(message = "ID счета обязателен")
    private UUID accountId;

    /**
     * Идентификатор пользователя
     */
    @NotNull(message = "ID пользователя обязателен")
    private UUID userId;

    /**
     * Сумма операции
     */
    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    /**
     * Код валюты
     */
    @NotBlank(message = "Валюта обязательна")
    private String currency;
}
