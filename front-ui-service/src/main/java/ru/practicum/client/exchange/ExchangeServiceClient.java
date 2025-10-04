package ru.practicum.client.exchange;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.exchange.dto.ExchangeRateDto;

/**
 * Клиент для сервиса обмена валют
 */
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

    /**
     * Получение текущих курсов валют от сервиса обмена
     */
    public Flux<ExchangeRateDto> getCurrentRates() {
        String path = "/api/exchange/rates";
        String operation = "Getting CurrentRates";
        String errorPrefix = "Ошибка текущих курсов валют: ";
        return performFlux(HttpMethod.GET, path, null, ExchangeRateDto.class, operation, errorPrefix, true)
                .doOnNext(account -> log.debug("Retrieved CurrentRates"));
    }
}