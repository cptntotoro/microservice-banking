package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.ExchangeRateDto;
import ru.practicum.mapper.ExchangeRateMapper;
import ru.practicum.service.ExchangeRateGeneratorService;

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

    @GetMapping("/rates/{base}/{target}")
    public Mono<ExchangeRateDto> getRate(@PathVariable String base,
                                         @PathVariable String target) {
        log.debug("Exchange Service requested rate: {} to {}", base, target);
        return generatorService.getRate(base, target)
                .map(exchangeRateMapper::exchangeRateToExchangeRateDto);
    }

    @GetMapping("/currencies")
    public Flux<String> getAvailableCurrencies() {
        return generatorService.getAvailableCurrencies();
    }
}