package ru.practicum.client.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для перевода
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDto {

    private UUID fromAccountId;

    private UUID toAccountId;

    @NotBlank(message = "Номер счета получателя обязателен для перевода на другой аккаунт")
    private String toAccountNumber;

    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}