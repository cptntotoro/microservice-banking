package ru.practicum.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExchangeRatesDto {
    private List<ExchangeRateDto> rates;
}