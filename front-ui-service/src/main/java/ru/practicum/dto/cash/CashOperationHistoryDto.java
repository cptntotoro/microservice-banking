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
public class CashOperationHistoryDto {
    private Long operationId;
    private String operationType;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime operationDate;
    private String status;
}
