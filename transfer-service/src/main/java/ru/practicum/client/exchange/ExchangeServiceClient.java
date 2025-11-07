package ru.practicum.client.exchange;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.exchange.dto.ExchangeRequestDto;
import ru.practicum.client.exchange.dto.ExchangeResponseDto;

/**
 * Клиент для сервиса обмена валют
 */
@Component
@Slf4j
public class ExchangeServiceClient extends BaseServiceClient {

    @Autowired
    public ExchangeServiceClient(@Qualifier("exchangeServiceWebClient") WebClient webClient) {
        super(webClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "exchange-service";
    }

    public Mono<ExchangeResponseDto> convertCurrency(ExchangeRequestDto requestDto) {
        String path = "/api/exchange/convert";
        String operation = "convertCurrency: " + requestDto;
        String errorPrefix = "Ошибка конвертации валют: ";
        return performMono(HttpMethod.POST, path, requestDto, ExchangeResponseDto.class, operation, errorPrefix, true)
                .doOnNext(account -> log.debug("Convert Currency"));
    }
}