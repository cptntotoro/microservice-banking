package ru.practicum.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для запроса на проведение транзакции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDto {

    /**
     * Идентификатор счета отправителя
     */
    @NotNull(message = "Счет отправителя обязателен для выбора")
    private UUID fromAccountId;

    /**
     * Идентификатор счета получателя
     */
    @NotNull(message = "Счет получателя обязателен для выбора")
    private UUID toAccountId;

    /**
     * Сумма транзакции
     */
    @NotNull(message = "Сумма обязательна для заполнения")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    /**
     * Описание транзакции
     */
    private String description;
}
