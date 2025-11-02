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
import ru.practicum.client.exchange.dto.ExchangeRateDto;

import java.util.List;

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

    public Mono<Void> sendExchangeRates(List<ExchangeRateDto> rates) {
        String path = "/api/exchange/update-rates";
        String operation = "Send exchange rates: " + rates;
        String errorPrefix = "Ошибка отправки обменных курсов валют: ";
        return performMono(HttpMethod.POST, path, rates, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Exchange rates sent"));
    }
}