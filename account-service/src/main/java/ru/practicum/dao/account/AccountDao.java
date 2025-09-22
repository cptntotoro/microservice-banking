package ru.practicum.dao.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO счета
 */
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDao {

    /**
     * Идентификатор
     */
    @Id
    private UUID id;

    /**
     * Идентификатор пользователя
     */
    @Column("user_id")
    private UUID userId;

    /**
     * Идентификатор валюты
     */
    @Column("currency_id")
    private UUID currencyId;

    /**
     * Баланс счета
     */
    private BigDecimal balance;

//    /**
//     * Номер счета
//     */
//    @Column("account_number")
//    private String accountNumber;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
