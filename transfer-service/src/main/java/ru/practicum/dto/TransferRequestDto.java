package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequestDto {
    private UUID fromAccountId;
    private UUID toAccountId;
    private String toAccountNumber; // Для перевода на другой аккаунт
    private BigDecimal amount;
}