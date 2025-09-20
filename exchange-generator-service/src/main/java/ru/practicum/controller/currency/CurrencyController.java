package ru.practicum.controller.currency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.practicum.dto.CurrencyDto;
import ru.practicum.mapper.currency.CurrencyMapper;
import ru.practicum.service.currency.CurrencyService;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Slf4j
public class CurrencyController {
    /**
     * Сервис генерации курсов обмена валют
     */
    private final CurrencyService currencyService;

    /**
     * Маппер курса обмена валюты
     */
    private final CurrencyMapper currencyMapper;

    @GetMapping("/available")
    public Flux<CurrencyDto> getAvailableCurrencies() {
        log.debug("Exchange Service requested available currencies");
        return currencyService.getAllCurrencies()
                .map(currencyMapper::currencyToCurrencyDto);
    }
}