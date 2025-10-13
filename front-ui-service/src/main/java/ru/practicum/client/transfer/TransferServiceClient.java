package ru.practicum.client.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.cash.dto.CashResponseClientDto;
import ru.practicum.client.transfer.dto.OtherTransferRequestClientDto;
import ru.practicum.client.transfer.dto.OwnTransferRequestClientDto;
import ru.practicum.exception.ServiceUnavailableException;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Клиент для обращений к сервису переводов
 */
@Component
@Slf4j
public class TransferServiceClient extends BaseServiceClient {

    @Autowired
    public TransferServiceClient(@Qualifier("transferServiceWebClient") WebClient webClient, DiscoveryClient discoveryClient) {
        super(webClient, discoveryClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "transfer-service";
    }

    public Mono<Void> performOwnTransfer(OwnTransferRequestClientDto requestDto) {
        String path = "/api/transfer/own";
        String operation = "Own Transfer: " + requestDto;
        String errorPrefix = "Ошибка перевода: ";
        return performMono(HttpMethod.POST, path, requestDto, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Deposit success: {}", response));
    }

    public Mono<Void> performOtherTransfer(OtherTransferRequestClientDto requestDto, UUID userId) {
        String path = "/api/transfer/other";
        String operation = "Other Transfer: " + requestDto;
        String errorPrefix = "Ошибка перевода: ";
        //TODO .header("X-User-Id", userId.toString())
        return performMono(HttpMethod.POST, path, requestDto, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Deposit success: {}", response));
    }
}