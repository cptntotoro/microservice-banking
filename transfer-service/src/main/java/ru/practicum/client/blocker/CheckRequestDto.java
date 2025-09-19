package ru.practicum.client.blocker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckRequestDto {
    private UUID fromId;
    private UUID toId;
    private BigDecimal amount;
    private boolean isOwn;
}