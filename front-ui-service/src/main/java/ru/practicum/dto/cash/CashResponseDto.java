package ru.practicum.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashResponseDto {
    /**
     * Идентификатор операции
     */
    private UUID operationId;

    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Тип операции
     */
    private String operationType; // DEPOSIT или WITHDRAW

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Новый баланс после операции
     */
    private BigDecimal newBalance;

    /**
     * Код валюты
     */
    private String currency;

    /**
     * Время и дата операции
     */
    private LocalDateTime operationDate;

    /**
     * Статус операции
     */
    private String status; // SUCCESS, FAILED

    /**
     * Сообщение (успех или ошибка)
     */
    private String message;
}
