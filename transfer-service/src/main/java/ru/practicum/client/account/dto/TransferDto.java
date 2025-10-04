package ru.practicum.client.account.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для перевода
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TransferDto {
    /**
     * Идентификатор счета отправителя
     */
    private UUID fromAccountId;

    /**
     * Идентификатор счета получателя
     */
    private UUID toAccountId;

    /**
     * Сумма операции
     */
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    /**
     * Конвертированная сумма для зачисления
     */
    private BigDecimal convertedAmount;
}