package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.OperationStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO ответа на перевод средств
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDto {
    /**
     * Статус операции
     */
    private OperationStatus status;

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
    private BigDecimal amount;

    /**
     * Сумма целевой операции
     */
    private BigDecimal convertedAmount;
}