package ru.practicum.client.cash;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.cash.dto.CashRequestClientDto;
import ru.practicum.client.cash.dto.CashResponseClientDto;

/**
 * Клиент для сервиса обналичивания денег
 */
@Component
@Slf4j
public class CashServiceClient extends BaseServiceClient {

    @Autowired
    public CashServiceClient(@Qualifier("cashServiceWebClient") WebClient webClient, DiscoveryClient discoveryClient) {
        super(webClient, discoveryClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "cash-service";
    }


    public Mono<CashResponseClientDto> deposit(CashRequestClientDto requestDto) {
        String path = "/cash/deposit";
        String operation = "Deposit: " + requestDto;
        String errorPrefix = "Ошибка депозита: ";
        return performMono(HttpMethod.POST, path, requestDto, CashResponseClientDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Deposit success: {}", response));
    }

    public Mono<CashResponseClientDto> withdraw(CashRequestClientDto requestDto) {
        String path = "/cash/withdraw";
        String operation = "Withdraw: " + requestDto;
        String errorPrefix = "Ошибка депозита: ";
        return performMono(HttpMethod.POST, path, requestDto, CashResponseClientDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Withdraw success: {}", response));
    }
}