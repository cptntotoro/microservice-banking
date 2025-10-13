package ru.practicum.dto.transfer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO перевода другому человеку
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherTransferRequestDto {

    /**
     * Идентификатор счета отправителя
     */
    @NotNull(message = "ID счета отправителя обязателен")
    private UUID fromAccountId;

    /**
     * Код целевой валюты
     */
    @NotNull(message = "Код валюты получателя обязателен")
    private String toCurrency;

    /**
     * Email получателя
     */
    @NotBlank(message = "Email получателя обязателен")
    @Email(message = "Некорректный формат email")
    private String recipientEmail;

    /**
     * Сумма операции
     */
    @NotNull(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;
}