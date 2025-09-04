package ru.practicum.model.transaction;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Транзакция
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Тип транзакции
     */
    private TransactionType type;

    /**
     * Идентификатор счета отправителя
     */
    private UUID fromAccountId;

    /**
     * Идентификатор счета получателя
     */
    private UUID toAccountId;

    /**
     * Сумма
     */
    private BigDecimal amount;

    /**
     * Идентификатор валюты
     */
    private UUID currencyId;

    /**
     * Описание
     */
    private String description;

    /**
     * Статус
     */
    private TransactionStatus status;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}
