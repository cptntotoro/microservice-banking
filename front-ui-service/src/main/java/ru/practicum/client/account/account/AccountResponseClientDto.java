package ru.practicum.client.account.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Клиентский DTO ответа с данными о счете пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseClientDto {
    private UUID id;
    private UUID userId;
    private String currencyCode;
    private String currencyName;
    private BigDecimal balance;
}
