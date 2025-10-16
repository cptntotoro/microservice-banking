package ru.practicum.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO операции обналичивания денег
 */
@Table(name = "cash_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CashOperationDao {
    /**
     * Идентификатор операции
     */
    @Id
    @Column("operation_uuid")
    private UUID operationUuid;

    /**
     * Идентификатор счета
     */
    @Column("account_id")
    private UUID accountId;

    /**
     * Тип операции
     */
    @Column("operation_type")
    private String operationType;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Код валюты
     */
    @Column("currency_code")
    private String currencyCode;

    /**
     * Статус
     */
    private String status;

    /**
     * Описание
     */
    private String description;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Дата завершения
     */
    @Column("completed_at")
    private LocalDateTime completedAt;
}