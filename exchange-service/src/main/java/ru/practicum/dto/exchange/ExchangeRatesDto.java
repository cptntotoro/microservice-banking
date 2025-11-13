package ru.practicum.dto.exchange;

import lombok.Data;

import java.util.List;

@Data
public class ExchangeRatesDto {
    private List<ExchangeRateDto> rates;
}