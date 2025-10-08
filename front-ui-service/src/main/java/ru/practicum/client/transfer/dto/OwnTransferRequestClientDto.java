package ru.practicum.client.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Клиентский DTO запроса на перевод средств между своими счетами
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnTransferRequestClientDto {
    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Идентификатор счета снятия
     */
    private UUID fromAccountId;

    /**
     * Идентификатор счета пополнения
     */
    private UUID toAccountId;

    /**
     * Сумма пополнения
     */
    private BigDecimal amount;
}