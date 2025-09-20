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
import ru.practicum.model.operation.OperationType;
import ru.practicum.service.exchange.ExchangeService;

import java.math.BigDecimal;

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
        log.info("Request for all current rates");
        return exchangeService.getCurrentRates()
                .map(exchangeRateMapper::exchangeRateToExchangeRateDto);
    }

    @GetMapping("/rates/{from}/{to}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ExchangeRateDto> getRate(@PathVariable String from, @PathVariable String to) {
        log.info("Request for rate: {} to {}", from, to);
        return exchangeService.getRate(from, to)
                .map(exchangeRateMapper::exchangeRateToExchangeRateDto);
    }

    @PostMapping("/convert")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ExchangeResponseDto> convertCurrency(@Valid @RequestBody ExchangeRequestDto requestDto) {
        log.info("Conversion request: {} to {}, amount {}, type {}, userId {}",
                requestDto.getFromCurrency(), requestDto.getToCurrency(), requestDto.getAmount(),
                requestDto.getOperationType(), requestDto.getUserId());
        return exchangeService.convert(
                requestDto.getFromCurrency(),
                requestDto.getToCurrency(),
                requestDto.getAmount(),
                requestDto.getOperationType(),
                requestDto.getUserId()
        ).flatMap(converted ->
                exchangeService.getRate(requestDto.getFromCurrency(), requestDto.getToCurrency())
                        .map(rate -> {
                            BigDecimal usedRate = (requestDto.getOperationType() == OperationType.BUY) ? rate.getBuyRate() : rate.getSellRate();
                            return exchangeRateMapper.toResponseDto(
                                    requestDto.getFromCurrency(),
                                    requestDto.getToCurrency(),
                                    requestDto.getAmount(),
                                    converted,
                                    usedRate
                            );
                        })
        );
    }

    @GetMapping("/currencies")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AvailableCurrenciesDto> getAvailableCurrencies() {
        log.info("Request for available currencies");
        return exchangeService.getAvailableCurrencies()
                .collectList()
                .map(currencies -> AvailableCurrenciesDto.builder()
                        .currencies(currencies)
                        .build());
    }
}