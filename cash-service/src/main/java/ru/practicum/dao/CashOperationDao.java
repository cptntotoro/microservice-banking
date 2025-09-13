package ru.practicum.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CashOperationDao {
    private UUID operationUuid;
    private UUID accountId;
    private String type;
    private BigDecimal amount;
    private String currencyCode;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
