package ru.practicum.dao;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "cash_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CashOperationDao {
    @Id
    @Column("operation_uuid")
    private UUID operationUuid;

    @Column("account_id")
    private UUID accountId;

    @Column("operation_type")
    private String operationType;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    @Column("currency_code")
    private String currencyCode;

    private String status;

    private String description;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("completed_at")
    private LocalDateTime completedAt;
}