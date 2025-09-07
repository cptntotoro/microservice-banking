package ru.practicum.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashOperationResponseDto {
    private Long operationId;
    private Long accountId;
    private String operationType; // DEPOSIT или WITHDRAW
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String currency;
    private String description;
    private LocalDateTime operationDate;
    private String status; // SUCCESS, FAILED, PENDING
}
