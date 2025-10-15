package ru.practicum.client.transfer.dto;

import lombok.*;

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
    private String status;

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