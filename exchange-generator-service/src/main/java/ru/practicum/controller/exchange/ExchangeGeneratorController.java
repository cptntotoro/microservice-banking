package ru.practicum.controller.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.practicum.client.exchange.dto.ExchangeRateDto;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.service.exchange.ExchangeRateGeneratorService;

@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
@Slf4j
public class ExchangeGeneratorController {
    /**
     * Сервис генерации курсов обмена валют
     */
    private final ExchangeRateGeneratorService generatorService;

    /**
     * Маппер курса обмена валюты
     */
    private final ExchangeRateMapper exchangeRateMapper;

    @GetMapping("/rates")
    public Flux<ExchangeRateDto> getCurrentRates() {
        log.debug("Exchange Service requested current rates");
        return generatorService.getCurrentRates()
                .map(exchangeRateMapper::exchangeRateToExchangeRateDto);
    }
}