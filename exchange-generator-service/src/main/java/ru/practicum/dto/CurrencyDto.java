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
     * Код
     */
    private String code;

    /**
     * Название
     */
    private String name;
}