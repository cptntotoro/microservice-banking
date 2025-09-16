package ru.practicum.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.practicum.model.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("operation_records")
public class OperationRecordDao {
    /**
     * Идентификатор
     */
    @Id
    private UUID id;

    /**
     * Идентификатор операции
     */
    private UUID operationId;

    /**
     * Тип операции
     */
    private OperationType operationType;

    /**
     * Идентификатор поьзователя
     */
    private UUID userId;

    /**
     * Идентификатор счета
     */
    private UUID accountId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Валюта операции
     */
    private String currency;

    /**
     * Дата и время операции
     */
    private LocalDateTime timestamp;

    /**
     * Дата и время записи
     */
    private LocalDateTime createdAt;

    /**
     * Флаг блокировки операции
     */
    private Boolean blocked;

    /**
     * Код причины блокировки
     */
    private String blockReasonCode;

    /**
     * Оценка риска
     */
    private Integer riskScore;
}
