package ru.practicum.controller.exchange;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.exchange.AvailableCurrenciesDto;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.dto.exchange.ExchangeRequestDto;
import ru.practicum.dto.exchange.ExchangeResponseDto;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.service.exchange.ExchangeService;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateController {
    /**
     * Сервис обмена валют
     */
    private final ExchangeService exchangeService;

    /**
     * Маппер курса обмена валют
     */
    private final ExchangeRateMapper exchangeRateMapper;

    @PostMapping("/update-rates")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> updateRates(@RequestBody Flux<ExchangeRateDto> ratesDto) {
        log.info("Received rate update from Generator");
        return exchangeService.updateRatesFromGenerator(
                ratesDto.map(exchangeRateMapper::exchangeRateDtoToExchangeRate)
        );
    }

    @GetMapping("/rates")
    public Flux<ExchangeRateDto> getCurrentRates() {
        log.info("Запрос всех текущих валют");
        return exchangeService.getCurrentRates()
                .map(exchangeRateMapper::exchangeRateToExchangeRateDto);
    }

    @GetMapping("/rates/{from}/{to}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ExchangeRateDto> getRate(@PathVariable String from, @PathVariable String to) {
        log.info("Запрос курса валюты: из {} в {}", from, to);
        return exchangeService.getRate(from, to)
                .map(exchangeRateMapper::exchangeRateToExchangeRateDto);
    }

    @PostMapping("/convert")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ExchangeResponseDto> convertCurrency(@Valid @RequestBody ExchangeRequestDto requestDto) {
        log.info("Запрос на конвертацию: {} в {}, сумма {}",
                requestDto.getFromCurrency(), requestDto.getToCurrency(), requestDto.getAmount());
        return exchangeService.convert(
                requestDto.getFromCurrency(),
                requestDto.getToCurrency(),
                requestDto.getAmount()
        ).map(converted -> exchangeRateMapper.toResponseDto(
                requestDto.getFromCurrency(),
                requestDto.getToCurrency(),
                requestDto.getAmount(),
                converted));
    }

    @GetMapping("/currencies")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AvailableCurrenciesDto> getAvailableCurrencies() {
        log.info("Запрос всех доступных валют");
        return exchangeService.getAvailableCurrencies()
                .collectList()
                .map(currencies -> AvailableCurrenciesDto.builder()
                        .currencies(currencies)
                        .build());
    }
}