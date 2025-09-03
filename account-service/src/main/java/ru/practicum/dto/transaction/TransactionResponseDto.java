package ru.practicum.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.transaction.TransactionStatus;
import ru.practicum.model.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для ответа с детализированной информацией о транзакции
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {

    private UUID id;
    private TransactionType type;
    private UUID fromAccountId;
    private UUID toAccountId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private UUID currencyId;
    private String currencyCode;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
