package ru.practicum.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.practicum.model.OperationStatus;
import ru.practicum.model.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO для хранения операций перевода в БД
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transfers")
public class TransferDao {
    /**
     * Идентификатор записи
     */
    @Id
    private UUID id;

    /**
     * Идентификатор счета отправителя
     */
    private UUID fromAccountId;

    /**
     * Идентификатор счета получателя
     */
    private UUID toAccountId;

    /**
     * Сумма операции
     */
    private BigDecimal amount;

    /**
     * Конвертированная сумма
     */
    private BigDecimal convertedAmount;

    /**
     * Валюта отправителя
     */
    private String fromCurrency;

    /**
     * Валюта получателя
     */
    private String toCurrency;

    /**
     * Время операции
     */
    private LocalDateTime timestamp;

    /**
     * Статус операции
     */
    private OperationStatus status;

    /**
     * Тип операции
     */
    private TransferType type;

    /**
     * Описание ошибки (если есть)
     */
    private String errorDescription;
}
