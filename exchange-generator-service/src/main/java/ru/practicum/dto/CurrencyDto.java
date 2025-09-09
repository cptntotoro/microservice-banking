package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO валюты
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDto {
    /**
     * Код валюты
     */
    private String code;

    /**
     * Название валюты
     */
    private String name;
}