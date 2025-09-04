package ru.practicum.dao.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.practicum.model.transaction.TransactionStatus;
import ru.practicum.model.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO транзакции
 */
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDao {

    /**
     * Идентификатор
     */
    @Id
    private UUID id;

    /**
     * Тип транзакции
     */
    private TransactionType type;

    /**
     * Идентификатор счета отправителя
     */
    @Column("from_account_id")
    private UUID fromAccountId;

    /**
     * Идентификатор счета получателя
     */
    @Column("to_account_id")
    private UUID toAccountId;

    /**
     * Сумма транзакции
     */
    private BigDecimal amount;

    /**
     * Идентификатор валюты
     */
    @Column("currency_id")
    private UUID currencyId;

    /**
     * Описание транзакции
     */
    private String description;

    /**
     * Статус транзакции
     */
    private TransactionStatus status;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
