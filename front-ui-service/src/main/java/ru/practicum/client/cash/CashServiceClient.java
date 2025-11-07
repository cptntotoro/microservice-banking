package ru.practicum.client.cash;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public CashServiceClient(@Qualifier("cashServiceWebClient") WebClient webClient) {
        super(webClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "cash-service";
    }

    public Mono<CashResponseClientDto> cashOperation(CashRequestClientDto requestDto) {
        String path = "/api/cash/cash-operation";
        String operation = "Cash-operation: " + requestDto;
        String errorPrefix = "Ошибка cash-operation: ";
        return performMono(HttpMethod.POST, path, requestDto, CashResponseClientDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Withdraw success: {}", response));
    }
}