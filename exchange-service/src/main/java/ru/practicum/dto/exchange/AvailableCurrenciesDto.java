package ru.practicum.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO доступных валют
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableCurrenciesDto {
    /**
     * Список 3-буквенных кодов валют
     */
    private List<String> currencies;
}