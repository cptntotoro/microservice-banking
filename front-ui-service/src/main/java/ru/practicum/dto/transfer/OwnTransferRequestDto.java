package ru.practicum.dto.transfer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO запроса на перевод средств между своими счетами
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnTransferRequestDto {

    /**
     * Идентификатор счета снятия
     */
    @NotNull(message = "Идентификатор счета снятия обязателен")
    private UUID fromAccountId;

    /**
     * Идентификатор счета пополнения
     */
    @NotNull(message = "Идентификатор счета пополнения обязателен")
    private UUID toAccountId;

    /**
     * Сумма пополнения
     */
    @NotNull(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;
}