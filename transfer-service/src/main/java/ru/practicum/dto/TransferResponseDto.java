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
public class TransferResponseDto {
    private String status;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
}