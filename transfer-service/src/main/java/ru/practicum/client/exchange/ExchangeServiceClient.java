package ru.practicum.client.exchange;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.exchange.dto.ExchangeRequestDto;
import ru.practicum.client.exchange.dto.ExchangeRateDto;
import ru.practicum.client.exchange.dto.ExchangeResponseDto;
import ru.practicum.exception.ServiceClientException;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class ExchangeServiceClient extends BaseServiceClient {

    @Autowired
    public ExchangeServiceClient(@Qualifier("exchangeServiceWebClient") WebClient webClient, DiscoveryClient discoveryClient) {
        super(webClient, discoveryClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "exchange-service";
    }

    public Mono<ExchangeRateDto> getRate(String fromCurrency, String toCurrency) {
        String path = "/api/exchange/rate?from=" + fromCurrency + "&to=" + toCurrency;
        String operation = "getRate: from=" + fromCurrency + " to=" + toCurrency;
        String errorPrefix = "Ошибка получения курса валют: ";
        return performMono(HttpMethod.GET, path, null, ExchangeRateDto.class, operation, errorPrefix, true)
                .doOnNext(account -> log.debug("Rate got"));
    }

    public Mono<ExchangeResponseDto> convertCurrency(ExchangeRequestDto requestDto) {
        String path = "/api/exchange/convert";
        String operation = "convertCurrency: " + requestDto;
        String errorPrefix = "Ошибка конвертации валют: ";
        return performMono(HttpMethod.POST, path, requestDto, ExchangeResponseDto.class, operation, errorPrefix, true)
                .doOnNext(account -> log.debug("Сonvert Currency"));
    }

    public Flux<ExchangeRateDto> getCurrentRates() {
        String path = "/api/exchange/rates";
        String operation = "Getting CurrentRates";
        String errorPrefix = "Ошибка текущих курсов валют: ";
        return performFlux(HttpMethod.GET, path, null, ExchangeRateDto.class, operation, errorPrefix, true)
                .doOnNext(account -> log.debug("Retrieved CurrentRates"));
    }
}