package ru.practicum.client.transfer;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.transfer.dto.OtherTransferRequestClientDto;
import ru.practicum.client.transfer.dto.OwnTransferRequestClientDto;
import ru.practicum.client.transfer.dto.TransferResponseDto;

/**
 * Клиент для обращений к сервису переводов
 */
@Component
@Slf4j
public class TransferServiceClient extends BaseServiceClient {

    @Autowired
    public TransferServiceClient(@Qualifier("transferServiceWebClient") WebClient webClient) {
        super(webClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "transfer-service";
    }

    public Mono<TransferResponseDto> performOwnTransfer(OwnTransferRequestClientDto requestDto) {
        String path = "/api/transfer/own";
        String operation = "Own Transfer: " + requestDto;
        String errorPrefix = "Ошибка перевода: ";
        return performMono(HttpMethod.POST, path, requestDto, TransferResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Deposit success: {}", response));
    }

    public Mono<TransferResponseDto> performOtherTransfer(OtherTransferRequestClientDto requestDto) {
        String path = "/api/transfer/other";
        String operation = "Other Transfer: " + requestDto;
        String errorPrefix = "Ошибка перевода: ";
        return performMono(HttpMethod.POST, path, requestDto, TransferResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Deposit success: {}", response));
    }
}