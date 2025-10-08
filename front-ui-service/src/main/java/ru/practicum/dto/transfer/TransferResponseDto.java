package ru.practicum.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDto {
    /**
     * Идентификатор перевода
     */
    private UUID transferId;

    /**
     * Идентификатор счета отправителя
     */
    private UUID fromAccountId;

    /**
     * Идентификатор счета получателя
     */
    private UUID toAccountId;

    /**
     * Код целевой валюты
     */
    private String toCurrency;

    /**
     * Email получателя
     */
    private String recipientEmail;

    /**
     * Сумма перевода
     */
    private BigDecimal amount;

    /**
     * Конвертированная сумма перевода
     */
    private BigDecimal convertedAmount;

    /**
     * Статус операции
     */
    private String status;

    /**
     * Дата и время выполнения
     */
    private LocalDateTime createdAt;

    /**
     * Сообщение
     */
    private String message;
}